package com.gymapp.sessions

import com.gymapp.network.WorkoutPlanResponse

fun canFinishSession(saving: Boolean) = !saving

data class SessionSetDraft(val exerciseId: String, val exerciseName: String, val setNumber: Int, val repetitions: String = "")

data class SessionDraftState(val planId: String, val planName: String, val sets: List<SessionSetDraft>) {
    fun updateRepetitions(index: Int, value: String) = copy(sets = sets.mapIndexed { current, item -> if (current == index) item.copy(repetitions = value) else item })
    fun validationMessage(): String? = if (sets.isEmpty() || sets.any { it.repetitions.toIntOrNull()?.let { value -> value <= 0 } != false }) "Registra repeticiones mayores que cero en cada serie" else null

    companion object {
        fun from(plan: WorkoutPlanResponse) = SessionDraftState(
            planId = plan.id,
            planName = plan.name,
            sets = plan.days.flatMap { day -> day.exercises.flatMap { exercise -> (1..exercise.sets).map { number -> SessionSetDraft(exercise.exerciseId, exercise.name, number) } } }
        )
    }
}
