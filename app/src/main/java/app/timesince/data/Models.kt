package app.timesince.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class CounterType { SINCE, COUNTDOWN }
enum class DisplayFormat { COMPACT, LONG, CLOCK, DAYS, HMS, DHM }

@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: CounterType,
    val eventAt: Long,
    val color: Long = 0xFF2E7D32,
    val isRunning: Boolean = true,
    /** Elapsed/remaining milliseconds captured when paused. */
    val frozenMillis: Long = 0,
    val format: DisplayFormat = DisplayFormat.COMPACT,
    val showSeconds: Boolean = true,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val notificationsEnabled: Boolean = false,
    /** Since: milestone in days. Countdown: advance reminder in days. */
    val notificationDays: Int = 1
)

@Entity(tableName = "restarts", foreignKeys = [ForeignKey(entity = Counter::class, parentColumns = ["id"], childColumns = ["counterId"], onDelete = ForeignKey.CASCADE)], indices = [Index("counterId")])
data class RestartRecord(@PrimaryKey(autoGenerate = true) val id: Long = 0, val counterId: Long, val previousEventAt: Long, val restartedAt: Long, val elapsedMillis: Long)

class Converters {
    @TypeConverter fun type(value: String) = CounterType.valueOf(value)
    @TypeConverter fun type(value: CounterType) = value.name
    @TypeConverter fun format(value: String) = DisplayFormat.valueOf(value)
    @TypeConverter fun format(value: DisplayFormat) = value.name
}

@Dao interface CounterDao {
    @Query("SELECT * FROM counters ORDER BY position, createdAt") fun observeAll(): Flow<List<Counter>>
    @Query("SELECT * FROM counters WHERE id=:id") fun observe(id: Long): Flow<Counter?>
    @Query("SELECT * FROM counters WHERE id=:id") suspend fun get(id: Long): Counter?
    @Insert suspend fun insert(counter: Counter): Long
    @Update suspend fun update(counter: Counter)
    @Delete suspend fun delete(counter: Counter)
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM counters") suspend fun nextPosition(): Int
    @Query("SELECT * FROM restarts WHERE counterId=:id ORDER BY restartedAt DESC") fun history(id: Long): Flow<List<RestartRecord>>
    @Insert suspend fun addRestart(record: RestartRecord)
    @Query("DELETE FROM restarts WHERE counterId=:id") suspend fun clearHistory(id: Long)
}

@Database(entities = [Counter::class, RestartRecord::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { abstract fun counters(): CounterDao }
