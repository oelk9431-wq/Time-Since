package app.timesince.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerMathTest {
    private val now = 2_000_000L
    @Test fun `time since uses timestamps`() = assertEquals(500_000, TimerMath.valueMillis(Counter(name="A", type=CounterType.SINCE, eventAt=1_500_000), now))
    @Test fun `countdown computes remaining`() = assertEquals(500_000, TimerMath.valueMillis(Counter(name="A", type=CounterType.COUNTDOWN, eventAt=2_500_000), now))
    @Test fun `paused counter uses frozen value`() = assertEquals(123_456, TimerMath.valueMillis(Counter(name="A", type=CounterType.SINCE, eventAt=0, isRunning=false, frozenMillis=123_456), now))
    @Test fun `countdown never becomes negative`() = assertEquals(0, TimerMath.valueMillis(Counter(name="A", type=CounterType.COUNTDOWN, eventAt=1_000_000), now))
    @Test fun `restart baseline starts at zero`() = assertEquals(0, TimerMath.valueMillis(Counter(name="A", type=CounterType.SINCE, eventAt=now), now))
    @Test fun `compact duration formatting`() = assertEquals("1d 02:03:04", TimerMath.format(93_784_000, DisplayFormat.COMPACT))
    @Test fun `format can hide seconds`() = assertEquals("1d 02:03", TimerMath.format(93_784_000, DisplayFormat.COMPACT, false))
    @Test fun `long duration formatting`() = assertEquals("1 days 2 hours 3 minutes 4 seconds", TimerMath.format(93_784_000, DisplayFormat.LONG))
}
