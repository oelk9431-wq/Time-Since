package app.timesince

import android.app.*
import android.content.Context
import androidx.room.Room
import app.timesince.data.*

class TimeSinceApp : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: CounterRepository
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "time-since.db").build()
        repository = CounterRepository(database)
        val channel = NotificationChannel(NotificationWorker.CHANNEL, "Counter reminders", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Milestones and countdown reminders" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
