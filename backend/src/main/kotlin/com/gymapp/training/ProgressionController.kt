package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class ExerciseProgressionResponse(val exerciseName: String, val previousRepetitions: Int, val latestRepetitions: Int, val previousLoadKg: Double, val latestLoadKg: Double, val action: String, val explanation: String)
data class PersonalRecordResponse(val exerciseName: String, val maximumLoadKg: Double?, val maximumLoadAt: String?, val maximumRepetitions: Int, val maximumRepetitionsAt: String)
data class ProgressAnalysisResponse(val periodDays: Int, val completedSessions: Int, val scheduledSessions: Int, val adherencePercent: Int?, val weightChangeKg: Double?, val activeGoals: Int, val goalsWithCurrentValue: Int, val recentPersonalRecords: Int, val sufficientData: Boolean, val sources: List<String>)

@RestController
@RequestMapping("/api/v1/training-progress")
class ProgressionController(private val service: ProgressionService) {
    @GetMapping("/recommendations") fun recommendations(@RequestAttribute("authenticatedUserId") userId: UUID) = service.recommendations(userId)
    @GetMapping("/personal-records") fun personalRecords(@RequestAttribute("authenticatedUserId") userId: UUID) = service.personalRecords(userId)
    @GetMapping("/analysis") fun analysis(@RequestAttribute("authenticatedUserId") userId: UUID) = service.analysis(userId)
}

@Service
class ProgressionService(private val jdbc: JdbcTemplate) {
    private data class Record(val name: String, val reps: Int, val load: Double)
    private data class PersonalRecordRow(val name: String, val startedAt: OffsetDateTime, val repetitions: Int, val loadKg: Double?)
    fun recommendations(userId: UUID): List<ExerciseProgressionResponse> = jdbc.query(
        "select e.name, s.started_at, sum(l.repetitions) reps, coalesce(avg(l.load_kg), 0) load from workout_set_logs l join workout_sessions s on s.id=l.session_id join workout_plans p on p.id=s.plan_id join exercises e on e.id=l.exercise_id where s.user_id=? and p.archived=false group by e.name, s.started_at order by e.name, s.started_at", { r, _ -> Record(r.getString("name"), r.getInt("reps"), r.getDouble("load")) }, userId
    ).groupBy { it.name }.mapNotNull { (_, rows) ->
        if (rows.size < 2) null else {
            val previous = rows[rows.lastIndex - 1]; val latest = rows.last()
            val action = when { latest.reps >= previous.reps && latest.load == previous.load -> "INCREASE"; latest.reps < previous.reps && latest.load == previous.load -> "REDUCE"; else -> "MAINTAIN" }
            val explanation = when (action) { "INCREASE" -> "Igualaste o superaste las repeticiones con la misma carga."; "REDUCE" -> "Hiciste menos repeticiones con la misma carga."; else -> "El cambio de carga requiere mantener la progresión actual." }
            ExerciseProgressionResponse(latest.name, previous.reps, latest.reps, previous.load, latest.load, action, explanation)
        }
    }.sortedBy { it.exerciseName }

    fun personalRecords(userId: UUID): List<PersonalRecordResponse> = jdbc.query(
        "select e.name, s.started_at, l.repetitions, l.load_kg from workout_set_logs l join workout_sessions s on s.id=l.session_id join exercises e on e.id=l.exercise_id where s.user_id=?", { row, _ ->
            PersonalRecordRow(row.getString("name"), row.getObject("started_at", OffsetDateTime::class.java), row.getInt("repetitions"), (row.getObject("load_kg") as? Number)?.toDouble())
        }, userId
    ).groupBy { it.name }.map { (name, rows) ->
        val repetitions = rows.maxWith(compareBy<PersonalRecordRow> { it.repetitions }.thenBy { it.startedAt })
        val load = rows.filter { it.loadKg != null }.maxWithOrNull(compareBy<PersonalRecordRow> { it.loadKg!! }.thenBy { it.startedAt })
        PersonalRecordResponse(name, load?.loadKg, load?.startedAt?.toString(), repetitions.repetitions, repetitions.startedAt.toString())
    }.sortedBy { it.exerciseName }

    fun analysis(userId: UUID, now: OffsetDateTime = OffsetDateTime.now()): ProgressAnalysisResponse {
        val periodDays = 28
        val start = now.minusDays(periodDays.toLong())
        val completedSessions = jdbc.queryForObject("select count(*) from workout_sessions where user_id=? and started_at>=?", Int::class.java, userId, start) ?: 0
        val scheduledSessions = jdbc.queryForObject("select count(*) * 4 from workout_plan_days d join workout_plans p on p.id=d.plan_id where p.user_id=? and p.archived=false", Int::class.java, userId) ?: 0
        val measurements = jdbc.query("select recorded_on, weight_kg from body_measurements where user_id=? and recorded_on>=? order by recorded_on", { row, _ -> row.getDouble("weight_kg") }, userId, LocalDate.now().minusDays(periodDays.toLong()))
        val weightChange = measurements.takeIf { it.size >= 2 }?.let { it.last() - it.first() }
        val activeGoals = jdbc.queryForObject("select count(*) from progress_goals where user_id=? and status='ACTIVE'", Int::class.java, userId) ?: 0
        val goalsWithCurrentValue = jdbc.queryForObject("select count(*) from progress_goals g where g.user_id=? and g.status='ACTIVE' and ((g.goal_type='BODY_WEIGHT' and exists (select 1 from body_measurements m where m.user_id=g.user_id)) or (g.goal_type='EXERCISE_LOAD' and exists (select 1 from workout_set_logs l join workout_sessions s on s.id=l.session_id join exercises e on e.id=l.exercise_id where s.user_id=g.user_id and e.name=g.exercise_name and l.load_kg is not null)))", Int::class.java, userId) ?: 0
        val recentPersonalRecords = personalRecords(userId).count { record ->
            listOfNotNull(record.maximumLoadAt, record.maximumRepetitionsAt).any { OffsetDateTime.parse(it) >= start }
        }
        val sources = buildList {
            add("$completedSessions sesiones sincronizadas en los últimos $periodDays días")
            add("$scheduledSessions sesiones programadas en tus rutinas activas")
            if (measurements.isNotEmpty()) add("${measurements.size} medidas corporales registradas")
            if (activeGoals > 0) add("$activeGoals objetivos activos ($goalsWithCurrentValue con valor actual)")
            if (recentPersonalRecords > 0) add("$recentPersonalRecords récords personales vigentes logrados en el período")
        }
        return ProgressAnalysisResponse(periodDays, completedSessions, scheduledSessions, if (scheduledSessions == 0) null else (completedSessions * 100 / scheduledSessions).coerceAtMost(100), weightChange, activeGoals, goalsWithCurrentValue, recentPersonalRecords, completedSessions > 0 || measurements.size >= 2 || activeGoals > 0, sources)
    }
}
