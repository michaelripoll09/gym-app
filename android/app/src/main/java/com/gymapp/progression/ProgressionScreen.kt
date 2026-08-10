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
import com.gymapp.network.RoutineReviewResponse

enum class ProgressionContent { LOADING, ERROR, EMPTY, READY }
data class ProgressionState(val items: List<ExerciseProgressionResponse> = emptyList(), val loading: Boolean = false, val error: String? = null) { fun content() = when { loading -> ProgressionContent.LOADING; error != null -> ProgressionContent.ERROR; items.isEmpty() -> ProgressionContent.EMPTY; else -> ProgressionContent.READY } }
enum class RoutineReviewContent { LOADING, ERROR, EMPTY, READY }
data class RoutineReviewState(val review: RoutineReviewResponse? = null, val loading: Boolean = false, val error: String? = null) { fun content() = when { loading -> RoutineReviewContent.LOADING; error != null -> RoutineReviewContent.ERROR; review?.state == "READY" -> RoutineReviewContent.READY; else -> RoutineReviewContent.EMPTY } }
@Composable fun RoutineReviewScreen(state: RoutineReviewState, onRetry: () -> Unit, onEdit: (String) -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Revisión de rutina", color = Color(0xFFB9F227), fontSize = 30.sp) }; item { Button(onClick = onBack) { Text("Volver") } }
        when (state.content()) {
            RoutineReviewContent.LOADING -> item { Text("Analizando tu rutina…", color = Color.LightGray) }
            RoutineReviewContent.ERROR -> { item { Text(state.error ?: "No pudimos cargar la revisión", color = Color(0xFFFF8A80)) }; item { Button(onClick = onRetry) { Text("Reintentar") } } }
            RoutineReviewContent.EMPTY -> item { Text(if (state.review?.state == "NO_ACTIVE_PLAN") "Activa una rutina para revisar sugerencias." else "Completa al menos dos sesiones de tu rutina activa para recibir sugerencias.", color = Color.LightGray) }
            RoutineReviewContent.READY -> state.review!!.suggestions.forEach { suggestion -> item { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("${suggestion.dayName} · ${suggestion.exerciseName}", color = Color.White); Text(suggestion.action, color = Color(0xFFB9F227)); Text(suggestion.explanation, color = Color.LightGray); suggestion.sources.forEach { Text(it, color = Color.LightGray) }; Button(onClick = { onEdit(state.review.activePlanId!!) }) { Text("Editar rutina manualmente") } } } } }
        }
    }
}
@Composable fun ProgressionScreen(state: ProgressionState, onRetry: () -> Unit, onReview: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Progresión guiada", color = Color(0xFFB9F227), fontSize = 30.sp) }; item { Button(onClick = onBack) { Text("Volver a progreso") } }; item { Button(onClick = onReview) { Text("Revisar rutina activa") } }
        when (state.content()) {
            ProgressionContent.LOADING -> item { Text("Analizando tus registros…", color = Color.LightGray) }
            ProgressionContent.ERROR -> { item { Text(state.error ?: "No pudimos cargar recomendaciones", color = Color(0xFFFF8A80)) }; item { Button(onClick = onRetry) { Text("Reintentar") } } }
            ProgressionContent.EMPTY -> item { Text("Completa al menos dos sesiones con el mismo ejercicio para recibir una recomendación.", color = Color.LightGray) }
            ProgressionContent.READY -> items(state.items, key = { it.exerciseName }) { item -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(item.exerciseName, color = Color.White, fontSize = 19.sp); Text("${item.previousRepetitions} reps · ${item.previousLoadKg} kg → ${item.latestRepetitions} reps · ${item.latestLoadKg} kg", color = Color.LightGray); Text(when(item.action) { "INCREASE" -> "Aumentar"; "REDUCE" -> "Reducir"; else -> "Mantener" }, color = Color(0xFFB9F227)); Text(item.explanation, color = Color.LightGray) } } }
        }
    }
}
