package com.gymapp.routines

import com.gymapp.network.ExerciseResponse

fun routineListKey(section: String, exerciseId: String) = "$section-$exerciseId"

data class RoutineExerciseDraft(
    val exercise: ExerciseResponse,
    val sets: Int = 3,
    val repetitions: Int = 10,
    val restSeconds: Int = 60
)

data class RoutineDraftState(
    val name: String = "",
    val scheduledDays: Set<String> = emptySet(),
    val exercises: List<RoutineExerciseDraft> = emptyList()
) {
    fun toggleDay(day: String): RoutineDraftState = copy(scheduledDays = if (day in scheduledDays) scheduledDays - day else scheduledDays + day)

    fun addExercise(exercise: ExerciseResponse): RoutineDraftState =
        if (exercises.any { it.exercise.id == exercise.id }) this else copy(exercises = exercises + RoutineExerciseDraft(exercise))

    fun updateExercise(exerciseId: String, sets: Int, repetitions: Int, restSeconds: Int): RoutineDraftState =
        copy(exercises = exercises.map { draft -> if (draft.exercise.id == exerciseId) draft.copy(sets = sets, repetitions = repetitions, restSeconds = restSeconds) else draft })

    fun validationMessage(): String? = when {
        name.isBlank() -> "Escribe un nombre para la rutina"
        scheduledDays.isEmpty() -> "Selecciona al menos un día"
        exercises.isEmpty() -> "Añade al menos un ejercicio"
        exercises.any { it.sets <= 0 || it.repetitions <= 0 || it.restSeconds <= 0 } -> "Completa series, repeticiones y descanso con valores mayores que cero"
        else -> null
    }
}
