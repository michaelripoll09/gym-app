package com.gymapp.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.WorkoutPlanResponse

@Composable
fun TodayTrainingScreen(
    state: TodayTrainingState,
    day: String,
    onStart: (WorkoutPlanResponse) -> Unit,
    onShowRoutines: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Entrenamiento de hoy", color = Color(0xFFB9F227), fontSize = 30.sp) }
        item { Text(day, color = Color.LightGray) }
        item { Button(onClick = onBack) { Text("Volver al catálogo") } }
        if (state.loading) item { Text("Cargando entrenamiento…", color = Color.LightGray) }
        state.error?.let { message ->
            item { Text(message, color = Color(0xFFFF8A80)) }
            item { Button(onClick = onRetry) { Text("Reintentar") } }
        }
        if (!state.loading && state.plans.isEmpty() && state.error == null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (state.hasActivePlan) "Tu rutina activa no tiene entrenamiento programado para hoy." else "No tienes una rutina activa. Elige una desde Mis rutinas.", color = Color.LightGray)
                    Button(onClick = onShowRoutines) { Text("Ir a Mis rutinas") }
                }
            }
        }
        items(state.plans, key = { it.id }) { plan ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.name, color = Color.White, fontSize = 20.sp)
                    plan.days.filter { it.name == day }.flatMap { it.exercises }.forEach { exercise ->
                        Text("${exercise.name} · ${exercise.sets}×${exercise.minRepetitions}-${exercise.maxRepetitions}", color = Color.LightGray)
                    }
                    Button(onClick = { onStart(plan) }) { Text("Iniciar rutina") }
                }
            }
        }
    }
}
