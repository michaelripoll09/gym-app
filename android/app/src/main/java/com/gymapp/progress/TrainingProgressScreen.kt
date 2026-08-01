package com.gymapp.progress

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

private val progressLime = Color(0xFFB9F227)
private val progressCard = Color(0xFF1C2022)

@Composable
fun TrainingProgressScreen(state: TrainingProgressState, pendingCount: Int, onRetry: () -> Unit, onHistory: () -> Unit, onProgression: () -> Unit, onMeasurements: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Progreso", color = progressLime, fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver a mis rutinas") } }
        item { Button(onClick = onHistory) { Text("Ver historial") } }
        item { Button(onClick = onProgression) { Text("Ver progresión guiada") } }
        item { Button(onClick = onMeasurements) { Text("Ver medidas corporales") } }
        if (pendingCount > 0) item { Text("$pendingCount sesión(es) pendiente(s) no se incluyen hasta sincronizarlas.", color = Color(0xFFFFD180)) }
        when (state.content()) {
            ProgressContent.LOADING -> item { Text("Calculando tu progreso…", color = Color.LightGray) }
            ProgressContent.ERROR -> {
                item { Text(state.error ?: "No pudimos cargar tu progreso", color = Color(0xFFFF8A80)) }
                item { Button(onClick = onRetry) { Text("Reintentar") } }
            }
            ProgressContent.EMPTY -> item { Text("Completa una sesión para ver tu progreso de los últimos siete días.", color = Color.LightGray) }
            ProgressContent.READY -> {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = progressCard), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Últimos 7 días", color = Color.White, fontSize = 20.sp)
                            Text("${state.summary.completedSessions} sesiones completadas", color = Color.LightGray)
                            Text("${state.summary.registeredSets} series registradas", color = Color.LightGray)
                            Text("${state.summary.totalRepetitions} repeticiones totales", color = Color.LightGray)
                        }
                    }
                }
                item { Text("Por ejercicio", color = Color.White, fontSize = 20.sp) }
                if (state.exerciseLoads.isEmpty()) item { Text("Aún no tienes cargas registradas por ejercicio.", color = Color.LightGray) }
                items(state.exerciseLoads, key = { it.exerciseName }) { progress ->
                    Card(colors = CardDefaults.cardColors(containerColor = progressCard), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(progress.exerciseName, color = Color.White, fontSize = 18.sp)
                            Text("Última carga: ${progress.latestLoadKg} kg", color = Color.LightGray)
                            Text("Carga máxima: ${progress.maximumLoadKg} kg", color = Color.LightGray)
                        }
                    }
                }
                item { Text("Sesiones recientes", color = Color.White, fontSize = 20.sp) }
                items(state.recentSessions, key = { it.id }) { session ->
                    Card(colors = CardDefaults.cardColors(containerColor = progressCard), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(session.planName, color = Color.White, fontSize = 18.sp)
                            Text("Fecha: ${session.startedAt}", color = Color.LightGray)
                            Text("${session.sets.size} series registradas", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
