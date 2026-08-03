package com.gymapp.home

import com.gymapp.network.ProgressGoalResponse
import com.gymapp.today.TodayTrainingState

fun dashboardTodayMessage(state: TodayTrainingState): String? = when {
    state.loading || state.plans.isNotEmpty() -> null
    state.hasActivePlan -> "Tu rutina activa no tiene entrenamiento programado para hoy."
    else -> "No tienes una rutina activa. Elige una desde Mis rutinas."
}

fun dashboardUsesCachedTraining(state: TodayTrainingState): Boolean =
    state.error != null && state.plans.isNotEmpty()

fun dashboardPrimaryGoal(goals: List<ProgressGoalResponse>): ProgressGoalResponse? =
    goals.firstOrNull { it.status == "ACTIVE" } ?: goals.firstOrNull()
