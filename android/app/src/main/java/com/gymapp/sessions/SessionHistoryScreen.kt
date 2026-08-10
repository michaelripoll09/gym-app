package com.gymapp.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.WorkoutSessionResponse
import com.gymapp.offline.PendingSession

private val historyLime = Color(0xFFB9F227)
private val historyCard = Color(0xFF1C2022)

@Composable
fun SessionHistoryScreen(
    state: SessionHistoryState,
    pending: List<PendingSession>,
    correction: SessionCorrectionDraftState?,
    savingCorrection: Boolean,
    correctionError: String?,
    onSelect: (WorkoutSessionResponse) -> Unit,
    onEdit: (WorkoutSessionResponse) -> Unit,
    onRepetitionsChanged: (Int, String) -> Unit,
    onLoadChanged: (Int, String) -> Unit,
    onEffortChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSaveCorrection: () -> Unit,
    onCancelCorrection: () -> Unit,
    onDelete: (WorkoutSessionResponse) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    if (state.selected != null) {
        if (correction != null) SessionCorrectionScreen(correction, savingCorrection, correctionError, onRepetitionsChanged, onLoadChanged, onEffortChanged, onNoteChanged, onSaveCorrection, onCancelCorrection)
        else SessionHistoryDetail(state.selected, savingCorrection, correctionError, onEdit, onDelete, onBack)
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Historial", color = historyLime, fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver a mis rutinas") } }
        if (pending.isNotEmpty()) item { Text("${pending.size} sesión(es) pendiente(s) de sincronización. No se incluyen en el progreso todavía.", color = Color(0xFFFFD180)) }
        when (state.content()) {
            HistoryContent.LOADING -> item { Text("Cargando historial…", color = Color.LightGray) }
            HistoryContent.ERROR -> {
                item { Text(state.error ?: "No pudimos cargar tu historial", color = Color(0xFFFF8A80)) }
                item { Button(onClick = onRetry) { Text("Reintentar") } }
            }
            HistoryContent.EMPTY -> item { Text("Aún no has completado sesiones. Inicia una rutina para ver tu progreso aquí.", color = Color.LightGray) }
            HistoryContent.LIST -> items(state.sessions, key = { it.id }) { session ->
                Card(colors = CardDefaults.cardColors(containerColor = historyCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(session.planName, color = Color.White, fontSize = 20.sp)
                        Text("Fecha: ${session.startedAt}", color = Color.LightGray)
                        Text("${session.sets.size} series registradas", color = Color.LightGray)
                        session.perceivedExertion?.let { Text("Esfuerzo percibido: $it/10", color = historyLime) }
                        Button(onClick = { onSelect(session) }) { Text("Ver detalle") }
                    }
                }
            }
            HistoryContent.DETAIL -> Unit
        }
    }
}

@Composable
private fun SessionHistoryDetail(session: WorkoutSessionResponse, saving: Boolean, error: String?, onEdit: (WorkoutSessionResponse) -> Unit, onDelete: (WorkoutSessionResponse) -> Unit, onBack: () -> Unit) {
    var sessionToDelete by remember(session.id) { mutableStateOf<WorkoutSessionResponse?>(null) }
    sessionToDelete?.let { deleting ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Eliminar sesion") },
            text = { Text("Esta accion elimina la sesion y actualiza tu progreso. No se puede deshacer.") },
            confirmButton = { Button(onClick = { sessionToDelete = null; onDelete(deleting) }, enabled = !saving) { Text("Eliminar") } },
            dismissButton = { Button(onClick = { sessionToDelete = null }) { Text("Cancelar") } },
        )
    }
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(session.planName, color = historyLime, fontSize = 30.sp) }
        item { Text("Fecha de inicio: ${session.startedAt}", color = Color.LightGray) }
        session.perceivedExertion?.let { effort -> item { Text("Esfuerzo percibido: $effort/10", color = historyLime) } }
        session.note?.takeIf { it.isNotBlank() }?.let { note -> item { Text("Nota: $note", color = Color.LightGray) } }
        item { Button(onClick = onBack) { Text("Volver al historial") } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = { onEdit(session) }, enabled = !saving) { Text("Corregir sesion") } }
        item { Button(onClick = { sessionToDelete = session }, enabled = !saving) { Text(if (saving) "Eliminando..." else "Eliminar sesion") } }
        itemsIndexed(session.sets, key = { index, set -> "${set.exerciseName}-$index" }) { _, set ->
            Card(colors = CardDefaults.cardColors(containerColor = historyCard), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(set.exerciseName, color = Color.White, fontSize = 18.sp)
                    Text("Repeticiones realizadas: ${set.repetitions}", color = Color.LightGray)
                    set.loadKg?.let { Text("Carga: $it kg", color = Color.LightGray) }
                }
            }
        }
    }
}

@Composable
private fun SessionCorrectionScreen(
    state: SessionCorrectionDraftState,
    saving: Boolean,
    error: String?,
    onRepetitionsChanged: (Int, String) -> Unit,
    onLoadChanged: (Int, String) -> Unit,
    onEffortChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Corregir sesion · ${state.planName}", color = historyLime, fontSize = 28.sp) }
        item { Text("Solo se corrigen los valores registrados; la rutina original no cambia.", color = Color.LightGray) }
        itemsIndexed(state.sets, key = { index, set -> "${set.exerciseId}-$index" }) { index, set ->
            Card(colors = CardDefaults.cardColors(containerColor = historyCard)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${set.exerciseName} · Serie ${set.setNumber}", color = Color.White)
                    OutlinedTextField(set.repetitions, { onRepetitionsChanged(index, it) }, label = { Text("Repeticiones realizadas") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                    OutlinedTextField(set.loadKg, { onLoadChanged(index, it) }, label = { Text("Carga (kg, opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                }
            }
        }
        item { OutlinedTextField(state.perceivedExertion, onEffortChanged, label = { Text("Esfuerzo percibido (1-10)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving) }
        item { OutlinedTextField(state.note, onNoteChanged, label = { Text("Nota privada (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !saving) }
        state.validationMessage()?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = onSave, enabled = !saving && state.validationMessage() == null, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando..." else "Guardar correccion") } }
        item { Button(onClick = onCancel, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") } }
    }
}
