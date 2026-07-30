package com.gymapp.today

import com.gymapp.network.WorkoutPlanResponse
import java.time.DayOfWeek

fun plansForToday(plans: List<WorkoutPlanResponse>, day: String): List<WorkoutPlanResponse> =
    plans.filter { plan -> plan.days.any { it.name == day } }

fun spanishDayName(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

data class TodayTrainingState(
    val loading: Boolean = true,
    val plans: List<WorkoutPlanResponse> = emptyList(),
    val error: String? = null
)

fun todayLoadError(previousPlans: List<WorkoutPlanResponse>) =
    TodayTrainingState(loading = false, plans = previousPlans, error = "No pudimos cargar tu entrenamiento de hoy")
