package com.gymapp.guided

import com.gymapp.network.CreateWorkoutPlanRequest
import com.gymapp.network.GuidedRoutineProposalResponse
import com.gymapp.network.WorkoutDayRequest
import com.gymapp.network.WorkoutPlanExerciseRequest

fun GuidedRoutineProposalResponse.toCreateWorkoutPlanRequest() = CreateWorkoutPlanRequest(
    name = name,
    days = days.map { day ->
        WorkoutDayRequest(day.name, day.exercises.map { exercise ->
            WorkoutPlanExerciseRequest(exercise.exerciseId, exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, exercise.restSeconds)
        })
    },
)

fun discardGuidedRoutine(): GuidedRoutineProposalResponse? = null
