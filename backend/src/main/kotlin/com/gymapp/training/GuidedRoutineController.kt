package com.gymapp.training

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class GuidedRoutineProposalResponse(
    val name: String,
    val explanation: String,
    val source: String,
    val days: List<WorkoutPlanDayResponse>,
)

private data class GuidedProfile(
    val primaryProfile: String,
    val experienceLevel: String,
    val goal: String,
    val availableDays: Int,
    val sessionDurationMinutes: Int,
)

private data class GuidedExercise(val id: UUID, val name: String)

class GuidedRoutineUnavailableException : RuntimeException()

@Service
class GuidedRoutineService(private val jdbc: JdbcTemplate) {
    fun proposal(userId: UUID): GuidedRoutineProposalResponse {
        val profile = jdbc.query(
            "select primary_profile, experience_level, goal, available_days_per_week, session_duration_minutes from training_profiles where user_id = ?",
            { rows, _ -> GuidedProfile(rows.getString("primary_profile"), rows.getString("experience_level"), rows.getString("goal"), rows.getInt("available_days_per_week"), rows.getInt("session_duration_minutes")) },
            userId,
        ).singleOrNull() ?: throw GuidedRoutineUnavailableException()
        if (profile.availableDays !in 1..7 || profile.sessionDurationMinutes !in 15..240) throw GuidedRoutineUnavailableException()

        val exercises = jdbc.query(
            "select e.id, e.name from exercises e join exercise_training_profiles p on p.exercise_id = e.id where e.published = true and p.profile_code = ? order by e.name limit 6",
            { rows, _ -> GuidedExercise(rows.getObject("id", UUID::class.java), rows.getString("name")) },
            profile.primaryProfile,
        )
        if (exercises.isEmpty()) throw GuidedRoutineUnavailableException()

        val prescription = prescription(profile.primaryProfile, profile.experienceLevel)
        val days = dayNames.take(profile.availableDays).mapIndexed { index, dayName ->
            val selected = (0 until minOf(3, exercises.size)).map { offset -> exercises[(index + offset) % exercises.size] }
            WorkoutPlanDayResponse(dayName, selected.map { exercise ->
                WorkoutPlanExerciseResponse(exercise.id, exercise.name, prescription.sets, prescription.minRepetitions, prescription.maxRepetitions, prescription.restSeconds)
            })
        }
        return GuidedRoutineProposalResponse(
            name = "Rutina guiada de ${profileLabel(profile.primaryProfile)}",
            explanation = "Propuesta inicial de ${profile.availableDays} días para ${goalLabel(profile.goal)}, con sesiones de hasta ${profile.sessionDurationMinutes} minutos. Ajústala según tu experiencia y disponibilidad.",
            source = "DETERMINISTIC_FALLBACK",
            days = days,
        )
    }

    private fun prescription(profile: String, level: String): Prescription = when (profile) {
        "POWERLIFTING" -> Prescription(4, 3, 5, 180)
        "CALISTHENICS" -> Prescription(3, 6, 10, 90)
        "RUNNING", "CROSSFIT" -> Prescription(3, 10, 12, 60)
        else -> if (level == "BEGINNER") Prescription(3, 8, 12, 90) else Prescription(4, 6, 10, 120)
    }

    private data class Prescription(val sets: Int, val minRepetitions: Int, val maxRepetitions: Int, val restSeconds: Int)
    private companion object {
        val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        fun profileLabel(value: String) = value.lowercase().replace('_', ' ')
        fun goalLabel(value: String) = value.lowercase().replace('_', ' ')
    }
}

@RestController
@RequestMapping("/api/v1/guided-routines")
class GuidedRoutineController(private val service: GuidedRoutineService) {
    @PostMapping("/proposal")
    fun proposal(@RequestAttribute("authenticatedUserId") userId: UUID) = service.proposal(userId)

    @ExceptionHandler(GuidedRoutineUnavailableException::class)
    fun unavailable() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
}
