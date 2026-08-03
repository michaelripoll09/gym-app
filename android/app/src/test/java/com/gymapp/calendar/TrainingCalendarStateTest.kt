package com.gymapp.calendar

import com.gymapp.network.CalendarDayResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class TrainingCalendarStateTest {
    @Test fun `moves between adjacent months`() {
        assertEquals(YearMonth.of(2026, 9), calendarMonthAfter(YearMonth.of(2026, 8)))
        assertEquals(YearMonth.of(2026, 7), calendarMonthBefore(YearMonth.of(2026, 8)))
    }
    @Test fun `keeps downloaded days after a recoverable error`() {
        val days = listOf(CalendarDayResponse("2026-08-03", true, false, "Fuerza", 3))
        assertTrue(calendarLoadError(days).days.single().completed)
    }
}
