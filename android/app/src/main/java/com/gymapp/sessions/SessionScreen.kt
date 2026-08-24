package com.gymapp.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.ProgressMilestoneResponse
import com.gymapp.network.ExerciseSessionReferenceResponse
import com.gymapp.network.ExerciseProgressionResponse
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(
    state: SessionDraftState,
    saving: Boolean,
    error: String?,
    onRepetitionsChanged: (Int, String) -> Unit,
    onLoadChanged: (Int, String) -> Unit,
    onPerceivedExertionChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    milestones: List<ProgressMilestoneResponse>? = null,
    onMilestonesShown: () -> Unit = {},
    references: List<ExerciseSessionReferenceResponse> = emptyList(),
    referencesLoading: Boolean = false,
    referencesError: String? = null,
    onRetryReferences: () -> Unit = {},
    onApplyReference: (String, ExerciseSessionReferenceResponse) -> Unit = { _, _ -> },
    recommendations: List<ExerciseProgressionResponse> = emptyList(),
    recommendationsLoading: Boolean = false,
    recommendationsError: String? = null,
    onRetryRecommendations: () -> Unit = {},
    onApplyRecommendation: (String, ExerciseProgressionResponse) -> Unit = { _, _ -> },
    onUndoRecommendation: (String) -> Unit = {},
) {
    if (milestones != null) {
        LaunchedEffect(milestones) { delay(2500); onMilestonesShown() }
        LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF273028))) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(milestoneSummaryTitle(milestones), color = Color(0xFFB9F227), fontSize = 26.sp)
                        Text("Guardaste una mejora verificable en tu historial.", color = Color.White)
                        milestones.forEach { milestone ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(milestone.exerciseName, color = Color.White, fontSize = 18.sp)
                                    Text(milestoneValueLabel(milestone), color = Color(0xFFB9F227))
                                }
                            }
                        }
                        Text("Continuaremos automáticamente.", color = Color.LightGray)
                        Button(onClick = onMilestonesShown, modifier = Modifier.fillMaxWidth()) { Text("Continuar ahora") }
                    }
                }
            }
        }
        return
    }
    var timerExercise by rememberSaveable { mutableStateOf("") }
    var timerConfigured by rememberSaveable { mutableStateOf(0) }
    var timerRemaining by rememberSaveable { mutableStateOf(0) }
    var timerStatus by rememberSaveable { mutableStateOf(RestTimerStatus.IDLE.name) }
    var recommendationReview by remember { mutableStateOf<SessionRecommendationReview?>(null) }
    LaunchedEffect(timerStatus, timerRemaining) { if (timerStatus == RestTimerStatus.RUNNING.name && timerRemaining > 0) { delay(1000); timerRemaining--; if (timerRemaining == 0) timerStatus = RestTimerStatus.FINISHED.name } }
    recommendationReview?.let { review ->
        val preview = review.preview()
        AlertDialog(
            onDismissRequest = { recommendationReview = review.cancel() },
            title = { Text("Aplicar recomendación") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Carga: ${preview.loadKg} kg")
                    Text("Repeticiones: ${preview.repetitions}")
                    Text(preview.explanation)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onApplyRecommendation(review.exerciseId, review.recommendation)
                    recommendationReview = null
                }) { Text("Aplicar al borrador") }
            },
            dismissButton = {
                Button(onClick = { recommendationReview = review.cancel() }) { Text("Cancelar") }
            },
        )
    }
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Entrenar · ${state.planName}", color = Color(0xFFB9F227), fontSize = 28.sp) }
        item { Button(onClick = onBack, enabled = !saving) { Text("Cancelar") } }
        referencesError?.let { message -> item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2424))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message, color = Color(0xFFFFB4AB))
                    Button(onClick = onRetryReferences, enabled = !referencesLoading) { Text("Reintentar referencias") }
                }
            }
        } }
        recommendationsError?.let { message -> item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2424))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message, color = Color(0xFFFFB4AB))
                    Button(onClick = onRetryRecommendations, enabled = !recommendationsLoading) { Text("Reintentar recomendaciones") }
                }
            }
        } }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF273028))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cierre de sesion", color = Color.White, fontSize = 20.sp)
                    Text("Opcional: registra como te sentiste al terminar.", color = Color.LightGray)
                    OutlinedTextField(value = state.perceivedExertion, onValueChange = onPerceivedExertionChanged, label = { Text("Esfuerzo percibido (1-10)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                    OutlinedTextField(value = state.note, onValueChange = onNoteChanged, label = { Text("Nota de la sesion (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                }
            }
        }
        if (timerStatus != RestTimerStatus.IDLE.name) item { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF273028))) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Descanso · $timerExercise", color = Color.White, fontSize = 20.sp); Text(if (timerStatus == RestTimerStatus.FINISHED.name) "Descanso terminado" else "${timerRemaining}s restantes", color = Color(0xFFB9F227), fontSize = 26.sp); if (timerStatus == RestTimerStatus.RUNNING.name) Button(onClick = { timerStatus = RestTimerStatus.PAUSED.name }) { Text("Pausar") }; if (timerStatus == RestTimerStatus.PAUSED.name) Button(onClick = { timerStatus = RestTimerStatus.RUNNING.name }) { Text("Reanudar") }; Button(onClick = { timerRemaining = timerConfigured; timerStatus = RestTimerStatus.RUNNING.name }) { Text("Reiniciar") }; Button(onClick = { timerRemaining = 0; timerStatus = RestTimerStatus.IDLE.name }) { Text("Omitir") } } } }
        itemsIndexed(state.sets, key = { index, set -> "${set.exerciseId}-$index" }) { index, set ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${set.exerciseName} \u00b7 Serie ${set.setNumber}", color = Color.White)
                    references.firstOrNull { it.exerciseId == set.exerciseId }?.let { Text("Referencia registrada \u00b7 \u00daltimo registro: ${it.repetitions} reps${it.loadKg?.let { load -> " \u00b7 $load kg" }.orEmpty()}", color = Color.LightGray) } ?: Text("A\u00fan no hay referencias para este ejercicio.", color = Color.LightGray)
                    val recommendation = sessionRecommendationFor(set.exerciseName, recommendations)
                    if (recommendation != null) {
                        Text("Recomendaci\u00f3n para la pr\u00f3xima ejecuci\u00f3n: ${when (recommendation.action) { "INCREASE" -> "Aumentar"; "REDUCE" -> "Reducir"; else -> "Mantener" }}", color = Color(0xFFB9F227))
                        Text("\u00daltimo registro analizado: ${recommendation.latestRepetitions} reps \u00b7 ${recommendation.latestLoadKg} kg", color = Color.LightGray)
                        Text(recommendation.explanation, color = Color.LightGray)
                        if (state.sets.indexOfFirst { it.exerciseId == set.exerciseId } == index && state.canApplyRecommendation(set.exerciseId, recommendation)) {
                            Button(onClick = { recommendationReview = state.reviewRecommendation(set.exerciseId, recommendation) }, enabled = !saving) {
                                Text("Revisar y aplicar recomendación")
                            }
                        }
                    } else if (!recommendationsLoading && recommendationsError == null) {
                        Text("No hay recomendaci\u00f3n disponible: falta historial suficiente.", color = Color.LightGray)
                    }
                    if (state.sets.indexOfFirst { it.exerciseId == set.exerciseId } == index && state.canUndoRecommendation(set.exerciseId)) {
                        Button(onClick = { onUndoRecommendation(set.exerciseId) }, enabled = !saving) {
                            Text("Deshacer recomendación aplicada")
                        }
                    }
                    sessionReferenceFor(set.exerciseId, references)?.let { reference ->
                        Text("Registro del ${reference.recordedAt.substringBefore('T')}", color = Color.LightGray)
                        if (state.sets.indexOfFirst { it.exerciseId == set.exerciseId } == index && state.canApplyReference(set.exerciseId, reference)) {
                            Button(onClick = { onApplyReference(set.exerciseId, reference) }, enabled = !saving) {
                                Text("Aplicar referencia a series vacías")
                            }
                        }
                    }
                    OutlinedTextField(set.repetitions, { onRepetitionsChanged(index, it) }, label = { Text("Repeticiones realizadas") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                    OutlinedTextField(set.loadKg, { onLoadChanged(index, it) }, label = { Text("Carga (kg, opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                    Button(onClick = { if (set.restSeconds > 0) { timerExercise = set.exerciseName; timerConfigured = set.restSeconds; timerRemaining = set.restSeconds; timerStatus = RestTimerStatus.RUNNING.name } }, enabled = !saving && set.restSeconds > 0) { Text("Completar serie · descansar ${set.restSeconds}s") }
                }
            }
        }
        state.validationMessage()?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = onFinish, enabled = canFinishSession(saving), modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando sesión…" else "Finalizar sesión") } }
    }
}
