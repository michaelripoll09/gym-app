package com.gymapp.progression

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.ExerciseProgressionResponse

enum class ProgressionContent { LOADING, ERROR, EMPTY, READY }
data class ProgressionState(val items: List<ExerciseProgressionResponse> = emptyList(), val loading: Boolean = false, val error: String? = null) { fun content() = when { loading -> ProgressionContent.LOADING; error != null -> ProgressionContent.ERROR; items.isEmpty() -> ProgressionContent.EMPTY; else -> ProgressionContent.READY } }
@Composable fun ProgressionScreen(state: ProgressionState, onRetry: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Progresión guiada", color = Color(0xFFB9F227), fontSize = 30.sp) }; item { Button(onClick = onBack) { Text("Volver a progreso") } }
        when (state.content()) {
            ProgressionContent.LOADING -> item { Text("Analizando tus registros…", color = Color.LightGray) }
            ProgressionContent.ERROR -> { item { Text(state.error ?: "No pudimos cargar recomendaciones", color = Color(0xFFFF8A80)) }; item { Button(onClick = onRetry) { Text("Reintentar") } } }
            ProgressionContent.EMPTY -> item { Text("Completa al menos dos sesiones con el mismo ejercicio para recibir una recomendación.", color = Color.LightGray) }
            ProgressionContent.READY -> items(state.items, key = { it.exerciseName }) { item -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(item.exerciseName, color = Color.White, fontSize = 19.sp); Text("${item.previousRepetitions} reps · ${item.previousLoadKg} kg → ${item.latestRepetitions} reps · ${item.latestLoadKg} kg", color = Color.LightGray); Text(when(item.action) { "INCREASE" -> "Aumentar"; "REDUCE" -> "Reducir"; else -> "Mantener" }, color = Color(0xFFB9F227)); Text(item.explanation, color = Color.LightGray) } } }
        }
    }
}
