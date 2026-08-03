package com.gymapp.guided

import com.gymapp.network.CreateWorkoutPlanRequest
import com.gymapp.network.ExerciseResponse
import com.gymapp.network.GuidedRoutineProposalResponse
import com.gymapp.network.WorkoutDayRequest
import com.gymapp.network.WorkoutPlanExerciseRequest

data class GuidedExerciseDraft(
    val exercise: ExerciseResponse,
    val sets: Int,
    val minRepetitions: Int,
    val maxRepetitions: Int,
    val restSeconds: Int,
)

data class GuidedDayDraft(val name: String, val exercises: List<GuidedExerciseDraft>)

data class GuidedRoutineDraft(val name: String, val explanation: String, val days: List<GuidedDayDraft>) {
    companion object {
        fun from(proposal: GuidedRoutineProposalResponse) = GuidedRoutineDraft(
            name = proposal.name,
            explanation = proposal.explanation,
            days = proposal.days.map { day ->
                GuidedDayDraft(day.name, day.exercises.map { exercise ->
                    GuidedExerciseDraft(ExerciseResponse(exercise.exerciseId, exercise.name, ""), exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, exercise.restSeconds)
                })
            },
        )
    }

    fun rename(value: String) = copy(name = value)
    fun updateExercise(dayIndex: Int, exerciseIndex: Int, sets: Int, minRepetitions: Int, maxRepetitions: Int, restSeconds: Int) = copy(days = days.mapIndexed { index, day ->
        if (index != dayIndex) day else day.copy(exercises = day.exercises.mapIndexed { exercisePosition, exercise ->
            if (exercisePosition == exerciseIndex) exercise.copy(sets = sets, minRepetitions = minRepetitions, maxRepetitions = maxRepetitions, restSeconds = restSeconds) else exercise
        })
    })
    fun replaceExercise(dayIndex: Int, exerciseIndex: Int, replacement: ExerciseResponse) = copy(days = days.mapIndexed { index, day ->
        if (index != dayIndex) day else day.copy(exercises = day.exercises.mapIndexed { exercisePosition, exercise -> if (exercisePosition == exerciseIndex) exercise.copy(exercise = replacement) else exercise })
    })
    fun removeExercise(dayIndex: Int, exerciseIndex: Int) = copy(days = days.mapIndexed { index, day -> if (index == dayIndex) day.copy(exercises = day.exercises.filterIndexed { position, _ -> position != exerciseIndex }) else day })
    fun addExercise(dayIndex: Int, exercise: ExerciseResponse) = copy(days = days.mapIndexed { index, day -> if (index == dayIndex && day.exercises.none { it.exercise.id == exercise.id }) day.copy(exercises = day.exercises + GuidedExerciseDraft(exercise, 3, 8, 12, 90)) else day })
    fun removeDay(dayIndex: Int) = copy(days = days.filterIndexed { index, _ -> index != dayIndex })
    fun addDay(name: String) = if (days.any { it.name == name }) this else copy(days = days + GuidedDayDraft(name, emptyList()))
    fun validationMessage(): String? = when {
        name.isBlank() -> "Escribe un nombre para la rutina"
        days.isEmpty() -> "Mantén al menos un día de entrenamiento"
        days.any { it.exercises.isEmpty() } -> "Cada día debe tener al menos un ejercicio"
        days.flatMap { it.exercises }.any { it.sets <= 0 || it.minRepetitions <= 0 || it.maxRepetitions < it.minRepetitions || it.restSeconds <= 0 } -> "Completa series, repeticiones y descanso con valores válidos"
        else -> null
    }
    fun toCreateWorkoutPlanRequest() = CreateWorkoutPlanRequest(name.trim(), days.map { day -> WorkoutDayRequest(day.name, day.exercises.map { exercise -> WorkoutPlanExerciseRequest(exercise.exercise.id, exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, exercise.restSeconds) }) })
}

fun GuidedRoutineProposalResponse.toCreateWorkoutPlanRequest() = GuidedRoutineDraft.from(this).toCreateWorkoutPlanRequest()
fun discardGuidedRoutine(): GuidedRoutineProposalResponse? = null
