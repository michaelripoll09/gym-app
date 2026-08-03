package com.gymapp.training

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

class PlanAccessDeniedException : RuntimeException()

@Service
class TrainingService(private val jdbc: JdbcTemplate) {
    fun listSessions(userId: UUID): List<WorkoutSessionResponse> = jdbc.query("select s.id, s.started_at, p.name, s.perceived_exertion, s.note from workout_sessions s join workout_plans p on p.id=s.plan_id where s.user_id=? order by s.started_at desc", { rows, _ ->
        val sessionId = rows.getObject("id", UUID::class.java)
        WorkoutSessionResponse(sessionId, rows.getString("name"), rows.getObject("started_at").toString(), jdbc.query("select e.name, l.repetitions, l.load_kg from workout_set_logs l join exercises e on e.id=l.exercise_id where l.session_id=? order by e.name", { setRows, _ -> SessionSetResponse(setRows.getString("name"), setRows.getInt("repetitions"), (setRows.getObject("load_kg") as? Number)?.toDouble()) }, sessionId), (rows.getObject("perceived_exertion") as? Number)?.toInt(), rows.getString("note"))
    }, userId)

    fun listPlans(userId: UUID, archived: Boolean = false): List<WorkoutPlanResponse> = jdbc.query("select p.id, p.name, exists (select 1 from active_workout_plans a where a.user_id=p.user_id and a.plan_id=p.id) as active from workout_plans p where p.user_id = ? and p.archived=? order by p.created_at desc", { planRows, _ ->
        WorkoutPlanResponse(
            id = planRows.getObject("id", UUID::class.java),
            name = planRows.getString("name"),
            days = jdbc.query("select id, name from workout_plan_days where plan_id = ? order by position", { dayRows, _ ->
                WorkoutPlanDayResponse(
                    name = dayRows.getString("name"),
                    exercises = jdbc.query("select e.id, e.name, pe.sets, pe.min_repetitions, pe.max_repetitions, pe.rest_seconds from workout_plan_exercises pe join exercises e on e.id = pe.exercise_id where pe.day_id = ? order by e.name", { exerciseRows, _ ->
                        WorkoutPlanExerciseResponse(
                            exerciseId = exerciseRows.getObject("id", UUID::class.java),
                            name = exerciseRows.getString("name"),
                            sets = exerciseRows.getInt("sets"),
                            minRepetitions = exerciseRows.getInt("min_repetitions"),
                            maxRepetitions = exerciseRows.getInt("max_repetitions"),
                            restSeconds = exerciseRows.getInt("rest_seconds")
                        )
                    }, dayRows.getObject("id", UUID::class.java))
                )
            }, planRows.getObject("id", UUID::class.java)),
            active = planRows.getBoolean("active")
        )
    }, userId, archived)

    @Transactional fun createPlan(userId: UUID, request: CreateWorkoutPlanRequest): UUID {
        require(request.name.isNotBlank() && request.days.isNotEmpty())
        val profile = jdbc.queryForObject("select primary_profile from training_profiles where user_id = ?", String::class.java, userId) ?: throw IllegalArgumentException()
        val planId = UUID.randomUUID(); jdbc.update("insert into workout_plans (id, user_id, name) values (?, ?, ?)", planId, userId, request.name)
        writePlanDays(planId, request, profile)
        return planId
    }
    @Transactional fun updatePlan(userId: UUID, planId: UUID, request: CreateWorkoutPlanRequest) {
        if (jdbc.queryForObject("select count(*) from workout_plans where id=? and user_id=?", Int::class.java, planId, userId) != 1) throw PlanAccessDeniedException()
        require(request.name.isNotBlank() && request.days.isNotEmpty())
        val profile = jdbc.queryForObject("select primary_profile from training_profiles where user_id = ?", String::class.java, userId) ?: throw IllegalArgumentException()
        jdbc.update("update workout_plans set name=? where id=?", request.name, planId)
        jdbc.update("delete from workout_plan_days where plan_id=?", planId)
        writePlanDays(planId, request, profile)
    }
    @Transactional fun archivePlan(userId: UUID, planId: UUID, archived: Boolean) {
        if (jdbc.update("update workout_plans set archived=? where id=? and user_id=?", archived, planId, userId) != 1) throw PlanAccessDeniedException()
        if (archived) jdbc.update("delete from active_workout_plans where user_id=? and plan_id=?", userId, planId)
    }
    @Transactional fun activatePlan(userId: UUID, planId: UUID) {
        val plan = jdbc.query("select archived from workout_plans where id=? and user_id=?", { rows, _ -> rows.getBoolean("archived") }, planId, userId)
        if (plan.isEmpty()) throw PlanAccessDeniedException()
        require(!plan.single())
        jdbc.update("insert into active_workout_plans (user_id, plan_id) values (?, ?) on conflict (user_id) do update set plan_id=excluded.plan_id", userId, planId)
    }
    private fun writePlanDays(planId: UUID, request: CreateWorkoutPlanRequest, profile: String) {
        request.days.forEachIndexed { position, day ->
            val dayId = UUID.randomUUID(); jdbc.update("insert into workout_plan_days (id, plan_id, name, position) values (?, ?, ?, ?)", dayId, planId, day.name, position)
            day.exercises.forEach { exercise ->
                val restSeconds = exercise.restSeconds ?: 60
                require(exercise.sets > 0 && exercise.minRepetitions > 0 && exercise.maxRepetitions >= exercise.minRepetitions && restSeconds > 0)
                require(jdbc.queryForObject("select count(*) from exercises e join exercise_training_profiles p on p.exercise_id=e.id where e.id=? and e.published=true and p.profile_code=?", Int::class.java, exercise.exerciseId, profile) == 1)
                jdbc.update("insert into workout_plan_exercises (id, day_id, exercise_id, sets, min_repetitions, max_repetitions, rest_seconds) values (?, ?, ?, ?, ?, ?, ?)", UUID.randomUUID(), dayId, exercise.exerciseId, exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, restSeconds)
            }
        }
    }
    @Transactional fun createSession(userId: UUID, planId: UUID, request: CreateWorkoutSessionRequest): UUID {
        if (jdbc.queryForObject("select count(*) from workout_plans where id=? and user_id=?", Int::class.java, planId, userId) != 1) throw PlanAccessDeniedException()
        require(request.sets.isNotEmpty() && request.sets.all { it.repetitions > 0 && (it.loadKg == null || it.loadKg >= 0) })
        require(request.perceivedExertion == null || request.perceivedExertion in 1..10)
        val sessionId=UUID.randomUUID(); jdbc.update("insert into workout_sessions (id, plan_id, user_id, perceived_exertion, note) values (?, ?, ?, ?, ?)", sessionId, planId, userId, request.perceivedExertion, request.note?.trim()?.takeIf { it.isNotEmpty() })
        request.sets.forEach { set -> jdbc.update("insert into workout_set_logs (id, session_id, exercise_id, repetitions, load_kg) values (?, ?, ?, ?, ?)", UUID.randomUUID(), sessionId, set.exerciseId, set.repetitions, set.loadKg) }; return sessionId
    }
}
