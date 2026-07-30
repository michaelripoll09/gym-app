package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class ExerciseProgressionResponse(val exerciseName: String, val previousRepetitions: Int, val latestRepetitions: Int, val previousLoadKg: Double, val latestLoadKg: Double, val action: String, val explanation: String)

@RestController
@RequestMapping("/api/v1/training-progress")
class ProgressionController(private val service: ProgressionService) {
    @GetMapping("/recommendations") fun recommendations(@RequestAttribute("authenticatedUserId") userId: UUID) = service.recommendations(userId)
}

@Service
class ProgressionService(private val jdbc: JdbcTemplate) {
    private data class Record(val name: String, val reps: Int, val load: Double)
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
}
