package com.gymapp.sessions

import com.gymapp.network.WorkoutPlanResponse
import com.gymapp.network.ProgressMilestoneResponse
import com.gymapp.network.ExerciseSessionReferenceResponse
import com.gymapp.network.WorkoutSessionResponse

fun canFinishSession(saving: Boolean) = !saving

fun milestoneSummaryTitle(milestones: List<ProgressMilestoneResponse>) = if (milestones.size == 1) "Nuevo récord personal" else "Nuevos récords personales"

fun milestoneValueLabel(milestone: ProgressMilestoneResponse) = when (milestone.type) {
    "LOAD" -> "Carga máxima: ${formatMilestoneValue(milestone.value)} kg"
    else -> "Máximo de repeticiones: ${formatMilestoneValue(milestone.value)}"
}

private fun formatMilestoneValue(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

fun sessionFeedbackError(value: String): String? =
    if (value.isBlank() || value.toIntOrNull() in 1..10) null else "Selecciona un esfuerzo entre 1 y 10"

fun sessionReferenceFor(exerciseId: String, references: List<ExerciseSessionReferenceResponse>) =
    references.firstOrNull { it.exerciseId == exerciseId }

fun sessionReferencesLoadError() =
    "No pudimos cargar tus referencias. Puedes reintentar sin interrumpir la sesión."

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
    fun applyReference(exerciseId: String, reference: ExerciseSessionReferenceResponse?): SessionDraftState {
        if (reference == null) return this
        return copy(sets = sets.map { set ->
            if (set.exerciseId != exerciseId || set.repetitions.isNotBlank() || set.loadKg.isNotBlank()) set else set.copy(
                repetitions = set.repetitions.ifBlank { reference.repetitions.toString() },
                loadKg = set.loadKg.ifBlank { reference.loadKg?.toString().orEmpty() },
            )
        })
    }
    fun canApplyReference(exerciseId: String, reference: ExerciseSessionReferenceResponse?) =
        reference != null && sets.any { set ->
            set.exerciseId == exerciseId && set.repetitions.isBlank() && set.loadKg.isBlank()
        }
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

data class SessionCorrectionDraftState(
    val sessionId: String,
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
        sets.any { it.loadKg.isNotBlank() && (it.loadKg.toDoubleOrNull()?.let { value -> value < 0 } != false) } -> "Registra una carga en kg valida o dejala vacia"
        sessionFeedbackError(perceivedExertion) != null -> sessionFeedbackError(perceivedExertion)
        note.trim().length > 500 -> "La nota no puede superar 500 caracteres"
        else -> null
    }

    companion object {
        fun from(session: WorkoutSessionResponse) = SessionCorrectionDraftState(
            sessionId = session.id,
            planName = session.planName,
            sets = session.sets.mapIndexed { index, set -> SessionSetDraft(set.exerciseId, set.exerciseName, index + 1, repetitions = set.repetitions.toString(), loadKg = set.loadKg?.toString().orEmpty()) },
            perceivedExertion = session.perceivedExertion?.toString().orEmpty(),
            note = session.note.orEmpty(),
        )
    }
}
