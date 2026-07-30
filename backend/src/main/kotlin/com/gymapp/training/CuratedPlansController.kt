package com.gymapp.training

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CuratedPlanResponse(
    val id: String,
    val name: String,
    val description: String,
    val primaryProfile: String,
    val experienceLevel: String,
    val goal: String,
    val days: List<WorkoutPlanDayResponse>,
)

private data class CuratedProfile(val primaryProfile: String, val experienceLevel: String, val goal: String, val availableDays: Int)
private data class CuratedExercise(val id: UUID, val name: String)

@RestController
@RequestMapping("/api/v1/curated-plans")
class CuratedPlansController(private val jdbc: JdbcTemplate, private val training: TrainingService) {
    @GetMapping
    fun list(@RequestAttribute("authenticatedUserId") userId: UUID): List<CuratedPlanResponse> = curatedPlan(userId)?.let(::listOf) ?: emptyList()

    @PostMapping("/{planId}/adopt")
    fun adopt(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: String): ResponseEntity<IdResponse> {
        val plan = requireNotNull(curatedPlan(userId))
        require(plan.id == planId)
        val request = CreateWorkoutPlanRequest(
            name = plan.name,
            days = plan.days.map { day ->
                WorkoutDayRequest(day.name, day.exercises.map { exercise ->
                    ExercisePlanRequest(exercise.exerciseId, exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, exercise.restSeconds)
                })
            },
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(IdResponse(training.createPlan(userId, request)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidPlan() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()

    private fun curatedPlan(userId: UUID): CuratedPlanResponse? {
        val profile = jdbc.query(
            "select primary_profile, experience_level, goal, available_days_per_week from training_profiles where user_id = ?",
            { rows, _ -> CuratedProfile(rows.getString("primary_profile"), rows.getString("experience_level"), rows.getString("goal"), rows.getInt("available_days_per_week")) },
            userId,
        ).singleOrNull() ?: return null
        val exercises = jdbc.query(
            "select e.id, e.name from exercises e join exercise_training_profiles p on p.exercise_id = e.id where e.published = true and p.profile_code = ? order by e.name limit 3",
            { rows, _ -> CuratedExercise(rows.getObject("id", UUID::class.java), rows.getString("name")) },
            profile.primaryProfile,
        )
        if (exercises.isEmpty()) return null
        val days = listOf("Lunes", "Miércoles", "Viernes").take(profile.availableDays.coerceIn(1, 3)).map { day ->
            WorkoutPlanDayResponse(day, exercises.map { exercise ->
                WorkoutPlanExerciseResponse(exercise.id, exercise.name, 3, 8, 12, 90)
            })
        }
        val id = "starter-${profile.primaryProfile.lowercase()}-${profile.experienceLevel.lowercase()}-${profile.goal.lowercase().replace('_', '-')}"
        return CuratedPlanResponse(
            id = id,
            name = "Base de ${profileLabel(profile.primaryProfile)}",
            description = "Plan curado para ${levelLabel(profile.experienceLevel)} enfocado en ${goalLabel(profile.goal)}.",
            primaryProfile = profile.primaryProfile,
            experienceLevel = profile.experienceLevel,
            goal = profile.goal,
            days = days,
        )
    }

    private fun profileLabel(value: String) = when (value) {
        "GENERAL_FITNESS" -> "fitness general"
        "BODYBUILDING" -> "culturismo"
        "POWERLIFTING" -> "powerlifting"
        "RUNNING" -> "running"
        "CROSSFIT" -> "crossfit"
        "CALISTHENICS" -> "calistenia"
        else -> value.lowercase().replace('_', ' ')
    }

    private fun levelLabel(value: String) = when (value) {
        "BEGINNER" -> "principiante"
        "INTERMEDIATE" -> "intermedio"
        "ADVANCED" -> "avanzado"
        else -> value.lowercase()
    }

    private fun goalLabel(value: String) = when (value) {
        "MUSCLE_GAIN" -> "ganar músculo"
        "FAT_LOSS" -> "perder grasa"
        "ENDURANCE" -> "resistencia"
        "SKILL" -> "técnica"
        else -> value.lowercase().replace('_', ' ')
    }
}
