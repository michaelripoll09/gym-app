package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class NextWeeklySessionResponse(val planName: String, val dayName: String)
data class WeeklyTrainingSummaryResponse(
    val completedSessions: Int,
    val scheduledSessions: Int,
    val adherencePercent: Int,
    val volumeKg: Double,
    val nextSession: NextWeeklySessionResponse?,
)

@RestController
@RequestMapping("/api/v1/training-summary")
class WeeklySummaryController(private val service: WeeklySummaryService) {
    @GetMapping("/weekly")
    fun weekly(@RequestAttribute("authenticatedUserId") userId: UUID) = service.weekly(userId)
}

@org.springframework.stereotype.Service
class WeeklySummaryService(private val jdbc: JdbcTemplate) {
    fun weekly(userId: UUID, today: LocalDate = LocalDate.now()): WeeklyTrainingSummaryResponse {
        val zone = ZoneId.systemDefault()
        val weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay(zone).toOffsetDateTime()
        val nextWeekStart = weekStart.plusDays(7)
        val activePlanId = jdbc.query("select plan_id from active_workout_plans where user_id=?", { rows, _ -> rows.getObject("plan_id", UUID::class.java) }, userId).firstOrNull()
        val completed = (if (activePlanId == null) jdbc.queryForObject("select count(*) from workout_sessions where user_id=? and started_at >= ? and started_at < ?", Int::class.java, userId, weekStart, nextWeekStart) else jdbc.queryForObject("select count(*) from workout_sessions where user_id=? and plan_id=? and started_at >= ? and started_at < ?", Int::class.java, userId, activePlanId, weekStart, nextWeekStart)) ?: 0
        val scheduled = (if (activePlanId == null) jdbc.queryForObject("select count(*) from workout_plan_days d join workout_plans p on p.id=d.plan_id where p.user_id=? and p.archived=false", Int::class.java, userId) else jdbc.queryForObject("select count(*) from workout_plan_days d join workout_plans p on p.id=d.plan_id where p.user_id=? and p.id=? and p.archived=false", Int::class.java, userId, activePlanId)) ?: 0
        val volume = if (activePlanId == null) jdbc.queryForObject("select coalesce(sum(l.repetitions * coalesce(l.load_kg, 0)), 0) from workout_set_logs l join workout_sessions s on s.id=l.session_id join workout_plans p on p.id=s.plan_id where s.user_id=? and p.archived=false and s.started_at >= ? and s.started_at < ?", java.math.BigDecimal::class.java, userId, weekStart, nextWeekStart)?.toDouble() ?: 0.0 else jdbc.queryForObject("select coalesce(sum(l.repetitions * coalesce(l.load_kg, 0)), 0) from workout_set_logs l join workout_sessions s on s.id=l.session_id join workout_plans p on p.id=s.plan_id where s.user_id=? and p.id=? and p.archived=false and s.started_at >= ? and s.started_at < ?", java.math.BigDecimal::class.java, userId, activePlanId, weekStart, nextWeekStart)?.toDouble() ?: 0.0
        val next = jdbc.query(if (activePlanId == null) "select p.name as plan_name, d.name as day_name, d.position from workout_plan_days d join workout_plans p on p.id=d.plan_id where p.user_id=? and p.archived=false order by p.created_at desc, d.position" else "select p.name as plan_name, d.name as day_name, d.position from workout_plan_days d join workout_plans p on p.id=d.plan_id where p.user_id=? and p.id=? and p.archived=false order by p.created_at desc, d.position", { rows, _ -> NextWeeklySessionResponse(rows.getString("plan_name"), rows.getString("day_name")) }, *listOfNotNull(userId, activePlanId).toTypedArray()).minByOrNull { daysUntil(today.dayOfWeek, it.dayName) }
        return WeeklyTrainingSummaryResponse(
            completedSessions = completed,
            scheduledSessions = scheduled,
            adherencePercent = if (scheduled == 0) 0 else (completed * 100 / scheduled).coerceAtMost(100),
            volumeKg = volume,
            nextSession = next,
        )
    }

    private fun daysUntil(today: DayOfWeek, name: String): Int {
        val scheduled = mapOf(
            "Lunes" to DayOfWeek.MONDAY, "Martes" to DayOfWeek.TUESDAY, "MiÃ©rcoles" to DayOfWeek.WEDNESDAY,
            "Jueves" to DayOfWeek.THURSDAY, "Viernes" to DayOfWeek.FRIDAY, "SÃ¡bado" to DayOfWeek.SATURDAY,
            "Domingo" to DayOfWeek.SUNDAY,
        )[name] ?: return 7
        return (scheduled.value - today.value + 7) % 7
    }
}
