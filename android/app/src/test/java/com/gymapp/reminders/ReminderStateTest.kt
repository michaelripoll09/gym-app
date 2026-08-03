package com.gymapp.reminders

import com.gymapp.network.WorkoutPlanDayResponse
import com.gymapp.network.WorkoutPlanResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ReminderStateTest {
    private val zone = ZoneId.of("UTC")
    private val mondayRoutine = WorkoutPlanResponse("plan-1", "Fuerza", listOf(WorkoutPlanDayResponse("Lunes", emptyList())), active = true)

    @Test fun `creates one upcoming reminder at the selected time for a scheduled routine`() {
        val reminders = upcomingReminders(listOf(mondayRoutine), ReminderSettings(enabled = true, hour = 18, minute = 30), Instant.parse("2026-08-02T12:00:00Z"), zone)
        assertEquals(1, reminders.size)
        assertEquals("Fuerza", reminders.single().planName)
        assertEquals(Instant.parse("2026-08-03T18:30:00Z"), reminders.single().triggerAt)
    }

    @Test fun `does not create reminders when disabled or when no day is scheduled`() {
        assertTrue(upcomingReminders(listOf(mondayRoutine), ReminderSettings(), Instant.parse("2026-08-02T12:00:00Z"), zone).isEmpty())
        assertTrue(upcomingReminders(listOf(WorkoutPlanResponse("plan-2", "Libre", emptyList())), ReminderSettings(enabled = true), Instant.now(), zone).isEmpty())
    }

    @Test fun `moves today reminder to next week when its selected time has passed`() {
        val todayPlan = WorkoutPlanResponse("plan-3", "Cardio", listOf(WorkoutPlanDayResponse("Domingo", emptyList())), active = true)
        val reminder = upcomingReminders(listOf(todayPlan), ReminderSettings(enabled = true, hour = 8), Instant.parse("2026-08-02T12:00:00Z"), zone).single()
        assertEquals(Instant.parse("2026-08-09T08:00:00Z"), reminder.triggerAt)
    }

    @Test fun `cancels existing alarms before applying changed reminder settings`() {
        val gateway = FakeAlarmGateway()
        ReminderScheduleController(gateway, zone).apply(listOf(mondayRoutine), ReminderSettings(enabled = true), Instant.parse("2026-08-02T12:00:00Z"))
        assertEquals(1, gateway.cancelCalls)
        assertEquals(1, gateway.scheduled.size)

        ReminderScheduleController(gateway, zone).apply(emptyList(), ReminderSettings(enabled = false), Instant.parse("2026-08-02T12:00:00Z"))
        assertEquals(2, gateway.cancelCalls)
        assertTrue(gateway.scheduled.isEmpty())
    }

    @Test fun `replaces the scheduled routine when active routines change`() {
        val gateway = FakeAlarmGateway()
        val controller = ReminderScheduleController(gateway, zone)
        val sundayRoutine = WorkoutPlanResponse("plan-2", "Cardio", listOf(WorkoutPlanDayResponse("Domingo", emptyList())), active = true)
        controller.apply(listOf(mondayRoutine), ReminderSettings(enabled = true), Instant.parse("2026-08-02T12:00:00Z"))
        controller.apply(listOf(sundayRoutine), ReminderSettings(enabled = true), Instant.parse("2026-08-02T12:00:00Z"))
        assertEquals(2, gateway.cancelCalls)
        assertEquals(listOf("Cardio"), gateway.scheduled.map { it.planName })
    }

    @Test fun `schedules reminders only for the active routine`() {
        val inactive = WorkoutPlanResponse("plan-2", "Cardio", listOf(WorkoutPlanDayResponse("Lunes", emptyList())))
        val reminders = upcomingReminders(listOf(mondayRoutine, inactive), ReminderSettings(enabled = true), Instant.parse("2026-08-02T12:00:00Z"), zone)
        assertEquals(listOf("Fuerza"), reminders.map { it.planName })
    }

    private class FakeAlarmGateway : ReminderAlarmGateway {
        var cancelCalls = 0
        val scheduled = mutableListOf<ScheduledReminder>()
        override fun cancelAll() { cancelCalls++; scheduled.clear() }
        override fun schedule(reminder: ScheduledReminder) { scheduled += reminder }
    }
}
