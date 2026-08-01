package com.gymapp.goals

import com.gymapp.network.ProgressGoalResponse
import java.time.LocalDate

data class GoalProgress(val remainingValue: Double, val label: String)

fun goalProgress(goal: ProgressGoalResponse): GoalProgress {
    val current = goal.currentValue ?: return GoalProgress(goal.targetValue, "Sin datos actuales")
    return GoalProgress(kotlin.math.abs(goal.targetValue - current), when {
        goal.status == "COMPLETED" -> "Completado"
        goal.targetValue == current -> "Objetivo alcanzado"
        else -> "En progreso"
    })
}

fun goalInputError(type: String, value: String, date: String, exercise: String): String? {
    val target = value.toDoubleOrNull()
    if (target == null || !target.isFinite() || target !in 1.0..1000.0) return "Indica un valor objetivo valido entre 1 y 1000."
    if (type == "EXERCISE_LOAD" && exercise.isBlank()) return "Indica el nombre del ejercicio."
    if (date.isBlank()) return null
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return "Indica una fecha valida en formato AAAA-MM-DD."
    return if (parsed.isBefore(LocalDate.now())) "La fecha objetivo no puede estar en el pasado." else null
}
