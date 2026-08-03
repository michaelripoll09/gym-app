package com.gymapp.calendar

import com.gymapp.network.CalendarDayResponse
import java.time.YearMonth

data class TrainingCalendarState(val days: List<CalendarDayResponse> = emptyList(), val loading: Boolean = true, val error: String? = null)
fun calendarMonthAfter(month: YearMonth) = month.plusMonths(1)
fun calendarMonthBefore(month: YearMonth) = month.minusMonths(1)
fun calendarLoadError(days: List<CalendarDayResponse>) = TrainingCalendarState(days = days, loading = false, error = "No pudimos actualizar tu calendario")
