package com.gymapp.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.ExerciseResponse
import com.gymapp.network.WorkoutPlanResponse

private val lime = Color(0xFFB9F227)
private val card = Color(0xFF1C2022)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun RoutineEditorScreen(
    draft: RoutineDraftState,
    catalog: List<ExerciseResponse>,
    saving: Boolean,
    error: String?,
    onDraftChanged: (RoutineDraftState) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Nueva rutina", color = lime, fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver al catálogo") } }
        item { OutlinedTextField(draft.name, { onDraftChanged(draft.copy(name = it)) }, label = { Text("Nombre de la rutina") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Text("Días programados", color = Color.White)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                routineEditorDays.forEach { day ->
                    Button(onClick = { onDraftChanged(draft.toggleDay(day)) }) { Text(if (day in draft.scheduledDays) "✓ ${day.take(2)}" else day.take(2)) }
                }
            }
        }
        item { Text("Ejercicios seleccionados", color = Color.White, fontSize = 20.sp) }
        items(draft.exercises, key = { routineListKey("selected", it.exercise.id) }) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = card)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.exercise.name, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField("Series", item.sets) { onDraftChanged(draft.updateExercise(item.exercise.id, it, item.repetitions, item.restSeconds)) }
                        NumberField("Reps", item.repetitions) { onDraftChanged(draft.updateExercise(item.exercise.id, item.sets, it, item.restSeconds)) }
                        NumberField("Descanso", item.restSeconds) { onDraftChanged(draft.updateExercise(item.exercise.id, item.sets, item.repetitions, it)) }
                    }
                }
            }
        }
        draft.validationMessage()?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = onSave, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando…" else "Guardar rutina") } }
        item { Text("Añade ejercicios del catálogo", color = Color.White, fontSize = 20.sp) }
        items(catalog, key = { routineListKey("catalog", it.id) }) { exercise ->
            Card(colors = CardDefaults.cardColors(containerColor = card)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(exercise.name, color = Color.White, modifier = Modifier.weight(1f))
                    Button(onClick = { onDraftChanged(draft.addExercise(exercise)) }, enabled = draft.exercises.none { it.exercise.id == exercise.id }) { Text("Añadir") }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NumberField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(value.toString(), { onValueChange(it.toIntOrNull() ?: 0) }, label = { Text(label) }, modifier = Modifier.weight(1f))
}

@Composable
fun RoutineListScreen(plans: List<WorkoutPlanResponse>, loading: Boolean, error: String?, archived: Boolean, pendingArchive: WorkoutPlanResponse?, activatingPlanId: String?, offline: Boolean, pendingCount: Int, onStart: (WorkoutPlanResponse) -> Unit, onEdit: (WorkoutPlanResponse) -> Unit, onArchive: (WorkoutPlanResponse) -> Unit, onConfirmArchive: () -> Unit, onCancelArchive: () -> Unit, onActivate: (WorkoutPlanResponse) -> Unit, onRestore: (WorkoutPlanResponse) -> Unit, onShowArchived: () -> Unit, onShowActive: () -> Unit, onHistory: () -> Unit, onProgress: () -> Unit, onShowReminders: () -> Unit, onShowPending: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(if (archived) "Rutinas archivadas" else "Mis rutinas", color = lime, fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver al catálogo") } }
        item { Button(onClick = onHistory) { Text("Historial") } }
        item { Button(onClick = onProgress) { Text("Progreso") } }
        if (!archived) item { Button(onClick = onShowReminders) { Text("Recordatorios") } }
        if (!archived) item { Button(onClick = onShowPending) { Text("Sesiones pendientes ($pendingCount)") } }
        if (offline && !archived) item { Text("Sin conexión: puedes registrar una sesión y se guardará para sincronizarla después.", color = Color(0xFFFFD180)) }
        item { Button(onClick = if (archived) onShowActive else onShowArchived) { Text(if (archived) "Ver activas" else "Ver archivadas") } }
        pendingArchive?.let { plan -> item { Text("¿Archivar ${plan.name}?"); Button(onClick = onConfirmArchive) { Text("Confirmar archivado") }; Button(onClick = onCancelArchive) { Text("Cancelar") } } }
        if (loading) item { Text("Cargando rutinas…", color = Color.LightGray) }
        error?.let { item { Text(it, color = Color(0xFFFF8A80)) } }
        if (!loading && error == null && plans.isEmpty()) item { Text("Aún no tienes rutinas. Crea la primera desde el catálogo.", color = Color.LightGray) }
        items(plans, key = { it.id }) { plan ->
            Card(colors = CardDefaults.cardColors(containerColor = card)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.name, color = Color.White, fontSize = 20.sp)
                    if (plan.active) Text("Rutina activa", color = lime)
                    plan.days.forEach { day ->
                        Text("${day.name}: ${day.exercises.joinToString { "${it.name} · ${it.sets}×${it.minRepetitions}-${it.maxRepetitions} · ${it.restSeconds}s" }}", color = Color.LightGray)
                    }
                    if (archived) Button(onClick = { onRestore(plan) }) { Text("Restaurar rutina") }
                    else {
                        if (!plan.active) Button(onClick = { onActivate(plan) }, enabled = activatingPlanId == null) { Text(if (activatingPlanId == plan.id) "Activandoâ€¦" else "Activar rutina") }
                        Button(onClick = { onStart(plan) }) { Text("Iniciar rutina") }
                        Button(onClick = { onEdit(plan) }) { Text("Editar rutina") }
                        Button(onClick = { onArchive(plan) }) { Text("Archivar rutina") }
                    }
                }
            }
        }
    }
}
