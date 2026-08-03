package com.gymapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.ProgressGoalResponse
import com.gymapp.network.WorkoutPlanResponse
import com.gymapp.summary.WeeklySummaryContent
import com.gymapp.summary.WeeklySummaryState
import com.gymapp.today.TodayTrainingState

@Composable
fun HomeDashboardScreen(
    today: TodayTrainingState,
    day: String,
    weeklySummary: WeeklySummaryState,
    goals: List<ProgressGoalResponse>,
    goalsLoading: Boolean,
    goalsError: String?,
    onStart: (WorkoutPlanResponse) -> Unit,
    onShowRoutines: () -> Unit,
    onShowCatalog: () -> Unit,
    onShowProgress: () -> Unit,
    onShowCalendar: () -> Unit,
    onShowReminders: () -> Unit,
    onRetryToday: () -> Unit,
    onRetrySummary: () -> Unit,
    onRetryGoals: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Tu entrenamiento", color = Color(0xFFB9F227), fontSize = 30.sp) }
        item { Text(day, color = Color.LightGray) }
        item {
            DashboardCard("Entrenamiento de hoy") {
                when {
                    today.loading -> Text("Cargando entrenamiento…", color = Color.LightGray)
                    today.error != null && !dashboardUsesCachedTraining(today) -> {
                        Text(today.error, color = Color(0xFFFF8A80))
                        Button(onClick = onRetryToday) { Text("Reintentar") }
                    }
                    else -> {
                        if (dashboardUsesCachedTraining(today)) Text("Mostrando tu última rutina guardada.", color = Color.LightGray)
                        val message = dashboardTodayMessage(today)
                        if (message != null) {
                            Text(message, color = Color.LightGray)
                            Button(onClick = onShowRoutines) { Text("Ir a Mis rutinas") }
                        } else today.plans.forEach { plan ->
                            Text(plan.name, color = Color.White, fontSize = 18.sp)
                            plan.days.filter { it.name == day }.flatMap { it.exercises }.forEach { exercise ->
                                Text("${exercise.name} · ${exercise.sets}×${exercise.minRepetitions}-${exercise.maxRepetitions}", color = Color.LightGray)
                            }
                            Button(onClick = { onStart(plan) }) { Text("Iniciar rutina") }
                        }
                    }
                }
            }
        }
        item {
            DashboardCard("Resumen semanal") {
                when (weeklySummary.content()) {
                    WeeklySummaryContent.LOADING -> Text("Calculando tu semana…", color = Color.LightGray)
                    WeeklySummaryContent.ERROR -> {
                        Text(weeklySummary.error ?: "No pudimos cargar el resumen", color = Color(0xFFFF8A80))
                        Button(onClick = onRetrySummary) { Text("Reintentar") }
                    }
                    WeeklySummaryContent.EMPTY -> Text("Completa una sesión para ver tu adherencia semanal.", color = Color.LightGray)
                    WeeklySummaryContent.READY -> weeklySummary.summary?.let { summary ->
                        Text("${summary.completedSessions} de ${summary.scheduledSessions} sesiones realizadas", color = Color.White)
                        Text("Adherencia: ${summary.adherencePercent}%", color = Color(0xFFB9F227))
                    }
                }
            }
        }
        item {
            DashboardCard("Objetivo de progreso") {
                when {
                    goalsLoading -> Text("Cargando objetivos…", color = Color.LightGray)
                    goalsError != null -> {
                        Text(goalsError, color = Color(0xFFFF8A80))
                        Button(onClick = onRetryGoals) { Text("Reintentar") }
                    }
                    dashboardPrimaryGoal(goals) == null -> Text("Crea un objetivo para seguir tu progreso.", color = Color.LightGray)
                    else -> dashboardPrimaryGoal(goals)?.let { goal ->
                        Text(if (goal.type == "BODY_WEIGHT") "Peso corporal" else goal.exerciseName.orEmpty(), color = Color.White)
                        Text("Meta: ${goal.targetValue} kg", color = Color.LightGray)
                    }
                }
            }
        }
        item {
            DashboardCard("Explorar") {
                Button(onClick = onShowRoutines) { Text("Mis rutinas") }
                Button(onClick = onShowCatalog) { Text("Catálogo de ejercicios") }
                Button(onClick = onShowProgress) { Text("Progreso") }
                Button(onClick = onShowCalendar) { Text("Calendario de adherencia") }
                Button(onClick = onShowReminders) { Text("Recordatorios") }
            }
        }
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontSize = 20.sp)
            content()
        }
    }
}
