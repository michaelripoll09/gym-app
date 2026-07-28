package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

class PlanAccessDeniedException : RuntimeException()

@Service
class TrainingService(private val jdbc: JdbcTemplate) {
    @Transactional fun createPlan(userId: UUID, request: CreateWorkoutPlanRequest): UUID {
        require(request.name.isNotBlank() && request.days.isNotEmpty())
        val profile = jdbc.queryForObject("select primary_profile from training_profiles where user_id = ?", String::class.java, userId)
        val planId = UUID.randomUUID(); jdbc.update("insert into workout_plans (id, user_id, name) values (?, ?, ?)", planId, userId, request.name)
        request.days.forEachIndexed { position, day ->
            val dayId = UUID.randomUUID(); jdbc.update("insert into workout_plan_days (id, plan_id, name, position) values (?, ?, ?, ?)", dayId, planId, day.name, position)
            day.exercises.forEach { exercise ->
                require(exercise.sets > 0 && exercise.minRepetitions > 0 && exercise.maxRepetitions >= exercise.minRepetitions)
                require(jdbc.queryForObject("select count(*) from exercises e join exercise_training_profiles p on p.exercise_id=e.id where e.id=? and e.published=true and p.profile_code=?", Int::class.java, exercise.exerciseId, profile) == 1)
                jdbc.update("insert into workout_plan_exercises (id, day_id, exercise_id, sets, min_repetitions, max_repetitions) values (?, ?, ?, ?, ?, ?)", UUID.randomUUID(), dayId, exercise.exerciseId, exercise.sets, exercise.minRepetitions, exercise.maxRepetitions)
            }
        }; return planId
    }
    @Transactional fun createSession(userId: UUID, planId: UUID, request: CreateWorkoutSessionRequest): UUID {
        if (jdbc.queryForObject("select count(*) from workout_plans where id=? and user_id=?", Int::class.java, planId, userId) != 1) throw PlanAccessDeniedException()
        require(request.sets.isNotEmpty() && request.sets.all { it.repetitions > 0 })
        val sessionId=UUID.randomUUID(); jdbc.update("insert into workout_sessions (id, plan_id, user_id) values (?, ?, ?)", sessionId, planId, userId)
        request.sets.forEach { set -> jdbc.update("insert into workout_set_logs (id, session_id, exercise_id, repetitions) values (?, ?, ?, ?)", UUID.randomUUID(), sessionId, set.exerciseId, set.repetitions) }; return sessionId
    }
}
