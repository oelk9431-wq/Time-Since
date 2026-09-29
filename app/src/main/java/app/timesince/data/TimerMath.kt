package app.timesince.data

import kotlin.math.max

object TimerMath {
    fun valueMillis(counter: Counter, now: Long = System.currentTimeMillis()): Long {
        if (!counter.isRunning) return max(0, counter.frozenMillis)
        return when (counter.type) {
            CounterType.SINCE -> max(0, now - counter.eventAt)
            CounterType.COUNTDOWN -> max(0, counter.eventAt - now)
        }
    }
    fun format(millis: Long, format: DisplayFormat, seconds: Boolean = true): String {
        val total = max(0, millis) / 1000
        val days = total / 86400; val hours = (total / 3600) % 24; val minutes = (total / 60) % 60; val secs = total % 60
        return when (format) {
            DisplayFormat.COMPACT -> if (seconds) "%dd %02d:%02d:%02d".format(days, hours, minutes, secs) else "%dd %02d:%02d".format(days, hours, minutes)
            DisplayFormat.LONG -> buildString { append("$days days $hours hours $minutes minutes"); if (seconds) append(" $secs seconds") }
            DisplayFormat.CLOCK -> if (seconds) "%02d:%02d:%02d".format(total / 3600, minutes, secs) else "%02d:%02d".format(total / 3600, minutes)
            DisplayFormat.DAYS -> "$days days"
            DisplayFormat.HMS -> if (seconds) "%02d:%02d:%02d".format(total / 3600, minutes, secs) else "%02d:%02d".format(total / 3600, minutes)
            DisplayFormat.DHM -> "%dd %02dh %02dm".format(days, hours, minutes)
        }
    }
}
