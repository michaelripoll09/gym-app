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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.ProgressMilestoneResponse
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
    LaunchedEffect(timerStatus, timerRemaining) { if (timerStatus == RestTimerStatus.RUNNING.name && timerRemaining > 0) { delay(1000); timerRemaining--; if (timerRemaining == 0) timerStatus = RestTimerStatus.FINISHED.name } }
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Entrenar · ${state.planName}", color = Color(0xFFB9F227), fontSize = 28.sp) }
        item { Button(onClick = onBack, enabled = !saving) { Text("Cancelar") } }
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
                    Text("${set.exerciseName} · Serie ${set.setNumber}", color = Color.White)
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
