package com.gymapp.reminders

import com.gymapp.network.WorkoutPlanResponse
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReminderSettings(val enabled: Boolean = false, val hour: Int = 18, val minute: Int = 0)

data class ScheduledReminder(val requestCode: Int, val planId: String, val planName: String, val dayName: String, val triggerAt: Instant)

interface ReminderAlarmGateway {
    fun cancelAll()
    fun schedule(reminder: ScheduledReminder)
}

class ReminderScheduleController(private val gateway: ReminderAlarmGateway, private val zone: ZoneId = ZoneId.systemDefault()) {
    fun apply(plans: List<WorkoutPlanResponse>, settings: ReminderSettings, now: Instant = Instant.now()) {
        gateway.cancelAll()
        upcomingReminders(plans, settings, now, zone).forEach(gateway::schedule)
    }
}

fun upcomingReminders(plans: List<WorkoutPlanResponse>, settings: ReminderSettings, now: Instant, zone: ZoneId = ZoneId.systemDefault()): List<ScheduledReminder> {
    if (!settings.enabled) return emptyList()
    val today = now.atZone(zone).toLocalDate()
    return plans.flatMap { plan ->
        plan.days.mapNotNull { day ->
            spanishDayToDayOfWeek(day.name)?.let { scheduledDay ->
                val date = nextDateFor(scheduledDay, today, now, settings, zone)
                ScheduledReminder((plan.id + day.name).hashCode(), plan.id, plan.name, day.name, date.atTime(settings.hour, settings.minute).atZone(zone).toInstant())
            }
        }
    }
}

private fun nextDateFor(day: DayOfWeek, today: LocalDate, now: Instant, settings: ReminderSettings, zone: ZoneId): LocalDate {
    var candidate = today.plusDays(((day.value - today.dayOfWeek.value + 7) % 7).toLong())
    if (candidate.atTime(settings.hour, settings.minute).atZone(zone).toInstant() <= now) candidate = candidate.plusDays(7)
    return candidate
}

private fun spanishDayToDayOfWeek(day: String): DayOfWeek? = when (day) {
    "Lunes" -> DayOfWeek.MONDAY
    "Martes" -> DayOfWeek.TUESDAY
    "Miércoles" -> DayOfWeek.WEDNESDAY
    "Jueves" -> DayOfWeek.THURSDAY
    "Viernes" -> DayOfWeek.FRIDAY
    "Sábado" -> DayOfWeek.SATURDAY
    "Domingo" -> DayOfWeek.SUNDAY
    else -> null
}
