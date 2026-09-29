package app.timesince

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import app.timesince.data.*
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong("id", -1); val app = applicationContext as TimeSinceApp
        val c = app.database.counters().get(id) ?: return Result.success()
        if (!c.notificationsEnabled || !c.isRunning) return Result.success()
        if (android.os.Build.VERSION.SDK_INT >= 33 && applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val intent = Intent(applicationContext, MainActivity::class.java).putExtra("counterId", id)
        val pending = PendingIntent.getActivity(applicationContext, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val text = if (c.type == CounterType.COUNTDOWN) "${c.name} is approaching" else "${c.name}: ${c.notificationDays}-day milestone"
        NotificationManagerCompat.from(applicationContext).notify(id.toInt(), NotificationCompat.Builder(applicationContext, CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Time Since").setContentText(text).setContentIntent(pending).setAutoCancel(true).build())
        return Result.success()
    }
    companion object {
        const val CHANNEL = "counter_reminders"
        fun schedule(context: Context, c: Counter) {
            WorkManager.getInstance(context).cancelUniqueWork("counter-${c.id}")
            if (!c.notificationsEnabled || !c.isRunning) return
            val day = TimeUnit.DAYS.toMillis(c.notificationDays.toLong())
            val at = if (c.type == CounterType.SINCE) c.eventAt + day else c.eventAt - day
            val delay = (at - System.currentTimeMillis()).coerceAtLeast(0)
            val work = OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(workDataOf("id" to c.id)).build()
            WorkManager.getInstance(context).enqueueUniqueWork("counter-${c.id}", ExistingWorkPolicy.REPLACE, work)
        }
    }
}
