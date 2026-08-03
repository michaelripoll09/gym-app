package com.gymapp.sessions

import com.gymapp.network.WorkoutPlanResponse

fun canFinishSession(saving: Boolean) = !saving

fun sessionFeedbackError(value: String): String? =
    if (value.isBlank() || value.toIntOrNull() in 1..10) null else "Selecciona un esfuerzo entre 1 y 10"

data class SessionSetDraft(val exerciseId: String, val exerciseName: String, val setNumber: Int, val restSeconds: Int = 0, val repetitions: String = "", val loadKg: String = "")

data class SessionDraftState(
    val planId: String,
    val planName: String,
    val sets: List<SessionSetDraft>,
    val perceivedExertion: String = "",
    val note: String = "",
) {
    fun updateRepetitions(index: Int, value: String) = copy(sets = sets.mapIndexed { current, item -> if (current == index) item.copy(repetitions = value) else item })
    fun updateLoadKg(index: Int, value: String) = copy(sets = sets.mapIndexed { current, item -> if (current == index) item.copy(loadKg = value) else item })
    fun updatePerceivedExertion(value: String) = copy(perceivedExertion = value)
    fun updateNote(value: String) = copy(note = value)
    fun validationMessage(): String? = when {
        sets.isEmpty() || sets.any { it.repetitions.toIntOrNull()?.let { value -> value <= 0 } != false } -> "Registra repeticiones mayores que cero en cada serie"
        sets.any { it.loadKg.isNotBlank() && (it.loadKg.toDoubleOrNull()?.let { value -> value < 0 } != false) } -> "Registra una carga en kg válida o déjala vacía"
        sessionFeedbackError(perceivedExertion) != null -> sessionFeedbackError(perceivedExertion)
        else -> null
    }

    companion object {
        fun from(plan: WorkoutPlanResponse, day: String? = null) = SessionDraftState(
            planId = plan.id,
            planName = plan.name,
            sets = plan.days.filter { day == null || it.name == day }.flatMap { currentDay -> currentDay.exercises.flatMap { exercise -> (1..exercise.sets).map { number -> SessionSetDraft(exercise.exerciseId, exercise.name, number, exercise.restSeconds) } } }
        )
    }
}
