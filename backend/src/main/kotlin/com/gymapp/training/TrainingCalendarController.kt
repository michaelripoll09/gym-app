package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

data class CalendarDayResponse(val date: String, val completed: Boolean, val scheduled: Boolean, val planName: String? = null, val setCount: Int = 0)

@RestController
@RequestMapping("/api/v1/training-calendar")
class TrainingCalendarController(private val jdbc: JdbcTemplate) {
    @GetMapping
    fun calendar(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestParam month: String, @RequestParam zone: String): List<CalendarDayResponse> {
        val target = YearMonth.parse(month)
        val zoneId = ZoneId.of(zone)
        val start = target.atDay(1).atStartOfDay(zoneId).toOffsetDateTime()
        val end = target.plusMonths(1).atDay(1).atStartOfDay(zoneId).toOffsetDateTime()
        val completed = jdbc.query("select s.started_at, p.name, count(l.id) as set_count from workout_sessions s join workout_plans p on p.id=s.plan_id left join workout_set_logs l on l.session_id=s.id where s.user_id=? and s.started_at>=? and s.started_at<? group by s.id,p.name,s.started_at", { rows, _ ->
            rows.getObject("started_at", java.time.OffsetDateTime::class.java).atZoneSameInstant(zoneId).toLocalDate() to (rows.getString("name") to rows.getInt("set_count"))
        }, userId, start, end).groupBy({ it.first }, { it.second }).mapValues { it.value.first() }
        val scheduled = jdbc.query("select d.name from active_workout_plans a join workout_plan_days d on d.plan_id=a.plan_id where a.user_id=?", { rows, _ -> rows.getString("name") }, userId).toSet()
        return (1..target.lengthOfMonth()).map { day ->
            val date = target.atDay(day)
            val session = completed[date]
            CalendarDayResponse(date.toString(), session != null, scheduled.contains(spanishDay(date.dayOfWeek)), session?.first, session?.second ?: 0)
        }
    }

    private fun spanishDay(day: DayOfWeek) = when (day) {
        DayOfWeek.MONDAY -> "Lunes"; DayOfWeek.TUESDAY -> "Martes"; DayOfWeek.WEDNESDAY -> "Miércoles"; DayOfWeek.THURSDAY -> "Jueves"; DayOfWeek.FRIDAY -> "Viernes"; DayOfWeek.SATURDAY -> "Sábado"; DayOfWeek.SUNDAY -> "Domingo"
    }
}
