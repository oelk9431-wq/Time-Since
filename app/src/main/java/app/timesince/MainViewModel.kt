package app.timesince

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.timesince.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val repo = (app as TimeSinceApp).repository
    private val settingsRepo = SettingsRepository(app)
    val counters = repo.counters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
    fun counter(id: Long) = repo.counter(id)
    fun history(id: Long) = repo.history(id)
    fun run(block: suspend () -> Unit) = viewModelScope.launch { block() }
    fun save(counter: Counter, isNew: Boolean = false) = run {
        val saved = if (isNew) counter.copy(id = repo.add(counter)) else { repo.update(counter); counter }
        NotificationWorker.schedule(getApplication(), saved)
    }
    fun setTheme(theme: ThemeMode) = run { settingsRepo.theme(theme) }
    fun setDefaults(format: DisplayFormat, color: Long) = run { settingsRepo.defaults(format, color) }
    fun reorderCounters(orderedIds: List<Long>) = run { repo.updateOrder(orderedIds) }
    fun setRunning(counter: Counter, running: Boolean) = run {
        repo.setRunning(counter, running, System.currentTimeMillis())
        repo.counter(counter.id).first()?.let { NotificationWorker.schedule(getApplication(), it) }
    }
    fun restart(counter: Counter) = run {
        repo.restart(counter, System.currentTimeMillis())
        repo.counter(counter.id).first()?.let { NotificationWorker.schedule(getApplication(), it) }
    }
}
