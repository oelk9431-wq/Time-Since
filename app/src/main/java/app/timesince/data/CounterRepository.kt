package app.timesince.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CounterRepository(private val db: AppDatabase) {
    private val dao = db.counters()
    val counters = dao.observeAll()
    fun counter(id: Long) = dao.observe(id)
    fun history(id: Long) = dao.history(id)
    suspend fun add(counter: Counter) = dao.insert(counter.copy(position = dao.nextPosition()))
    suspend fun update(counter: Counter) = dao.update(counter)
    suspend fun delete(counter: Counter) = dao.delete(counter)
    suspend fun clearHistory(id: Long) = dao.clearHistory(id)
    suspend fun restart(counter: Counter, now: Long) = db.withTransaction {
        val elapsed = TimerMath.valueMillis(counter, now)
        dao.addRestart(RestartRecord(counterId = counter.id, previousEventAt = counter.eventAt, restartedAt = now, elapsedMillis = elapsed))
        dao.update(counter.copy(eventAt = now, frozenMillis = 0, isRunning = true))
    }
    suspend fun setRunning(counter: Counter, running: Boolean, now: Long) {
        if (running == counter.isRunning) return
        val changed = if (!running) counter.copy(isRunning = false, frozenMillis = TimerMath.valueMillis(counter, now))
        else if (counter.type == CounterType.SINCE) counter.copy(isRunning = true, eventAt = now - counter.frozenMillis)
        else counter.copy(isRunning = true, eventAt = now + counter.frozenMillis)
        dao.update(changed)
    }
    suspend fun move(counter: Counter, direction: Int) {
        val all = countersSnapshot()
        val index = all.indexOfFirst { it.id == counter.id }
        val other = all.getOrNull(index + direction) ?: return
        db.withTransaction { dao.update(counter.copy(position = other.position)); dao.update(other.copy(position = counter.position)) }
    }
    /** Persists a complete ordering in one transaction, producing unique positions. */
    suspend fun updateOrder(orderedIds: List<Long>) = db.withTransaction {
        val byId = counters.first().associateBy(Counter::id)
        orderedIds.forEachIndexed { position, id ->
            byId[id]?.let { counter ->
                if (counter.position != position) dao.update(counter.copy(position = position))
            }
        }
    }
    private suspend fun countersSnapshot(): List<Counter> = counters.first()
}
