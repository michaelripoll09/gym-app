package com.gymapp.sessions

import org.junit.Assert.assertEquals
import org.junit.Test

class RestTimerStateTest {
    @Test fun `starts pauses resumes skips and completes a configured rest`() {
        var timer = RestTimerState.start("Sentadilla", 90)
        assertEquals(RestTimerStatus.RUNNING, timer.status)
        timer = timer.tick(30).pause(); assertEquals(60, timer.remainingSeconds); assertEquals(RestTimerStatus.PAUSED, timer.status)
        timer = timer.resume().tick(60); assertEquals(RestTimerStatus.FINISHED, timer.status)
        assertEquals(RestTimerStatus.IDLE, timer.restart().skip().status)
    }
    @Test fun `does not start when no rest is configured`() { assertEquals(RestTimerStatus.IDLE, RestTimerState.start("Dominada", 0).status) }
}
