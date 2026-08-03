package com.gymapp.guided

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
import com.gymapp.network.ExerciseResponse

private val lime = Color(0xFFB9F227)
private val card = Color(0xFF1C2022)
private val routineDays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@Composable
fun GuidedRoutineScreen(
    loading: Boolean,
    draft: GuidedRoutineDraft?,
    catalog: List<ExerciseResponse>,
    error: String?,
    saving: Boolean,
    onGenerate: () -> Unit,
    onDraftChanged: (GuidedRoutineDraft) -> Unit,
    onConfirm: (GuidedRoutineDraft) -> Unit,
    onDiscard: () -> Unit,
    onBack: () -> Unit,
) {
    var picker by remember { mutableStateOf<Pair<Int, Int?>?>(null) }
    var pickerQuery by remember { mutableStateOf("") }
    if (picker != null && draft != null) {
        val (dayIndex, exerciseIndex) = picker!!
        val matches = filterCompatibleExercises(catalog, pickerQuery)
        LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(if (exerciseIndex == null) "Añadir ejercicio compatible" else "Sustituir ejercicio", color = lime, fontSize = 28.sp) }
            item { OutlinedTextField(pickerQuery, { pickerQuery = it }, label = { Text("Buscar ejercicio") }, modifier = Modifier.fillMaxWidth()) }
            if (matches.isEmpty()) item { Text("No hay coincidencias compatibles.", color = Color.LightGray) }
            items(matches, key = { it.id }) { exercise ->
                Button(onClick = {
                    onDraftChanged(if (exerciseIndex == null) draft.addExercise(dayIndex, exercise) else draft.replaceExercise(dayIndex, exerciseIndex, exercise))
                    picker = null
                }) { Text(exercise.name) }
            }
            item { Button(onClick = { picker = null }) { Text("Cancelar") } }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Rutina guiada", color = lime, fontSize = 30.sp) }
        item { Text("Esta es una propuesta editable: nada se guardará hasta que pulses Crear esta rutina.", color = Color.LightGray) }
        when {
            loading -> item { Text("Preparando tu propuesta…", color = Color.LightGray) }
            draft != null -> {
                item { OutlinedTextField(draft.name, { onDraftChanged(draft.rename(it)) }, label = { Text("Nombre de la rutina") }, modifier = Modifier.fillMaxWidth()) }
                item { Text(draft.explanation, color = Color.LightGray) }
                items(draft.days.indices.toList(), key = { index -> "${draft.days[index].name}-$index" }) { dayIndex ->
                    val day = draft.days[dayIndex]
                    Card(colors = CardDefaults.cardColors(containerColor = card), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(day.name, color = Color.White, fontSize = 20.sp)
                            day.exercises.forEachIndexed { exerciseIndex, exercise ->
                                Text(exercise.exercise.name, color = Color.White)
                                ExerciseInputs(exercise, onChange = { sets, min, max, rest -> onDraftChanged(draft.updateExercise(dayIndex, exerciseIndex, sets, min, max, rest)) })
                                Button(onClick = { picker = dayIndex to exerciseIndex }) { Text("Sustituir ejercicio") }
                                Button(onClick = { onDraftChanged(draft.removeExercise(dayIndex, exerciseIndex)) }) { Text("Quitar ejercicio") }
                            }
                            Button(onClick = { picker = dayIndex to null }) { Text("Añadir ejercicio") }
                            Button(onClick = { onDraftChanged(draft.removeDay(dayIndex)) }) { Text("Quitar día") }
                        }
                    }
                }
                item { Text("Añadir día", color = lime) }
                items(routineDays.filter { candidate -> draft.days.none { it.name == candidate } }) { day -> Button(onClick = { onDraftChanged(draft.addDay(day)) }) { Text(day) } }
                item { error?.let { Text(it, color = Color(0xFFFF8A80)) } }
                item { draft.validationMessage()?.let { Text(it, color = Color(0xFFFF8A80)) } }
                item { Button(onClick = { onConfirm(draft) }, enabled = !saving && draft.validationMessage() == null) { Text(if (saving) "Creando rutina…" else "Crear esta rutina") } }
                item { Button(onClick = onDiscard, enabled = !saving) { Text("Descartar propuesta") } }
            }
            error != null -> {
                item { Text(error, color = Color(0xFFFF8A80)) }
                item { Button(onClick = onGenerate) { Text("Reintentar") } }
            }
            else -> item { Button(onClick = onGenerate) { Text("Generar propuesta") } }
        }
        item { Button(onClick = onBack, enabled = !saving) { Text("Volver al catálogo") } }
    }
}

@Composable
private fun ExerciseInputs(exercise: GuidedExerciseDraft, onChange: (Int, Int, Int, Int) -> Unit) {
    OutlinedTextField(exercise.sets.toString(), { value -> value.toIntOrNull()?.let { onChange(it, exercise.minRepetitions, exercise.maxRepetitions, exercise.restSeconds) } }, label = { Text("Series") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(exercise.minRepetitions.toString(), { value -> value.toIntOrNull()?.let { onChange(exercise.sets, it, exercise.maxRepetitions, exercise.restSeconds) } }, label = { Text("Repeticiones mínimas") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(exercise.maxRepetitions.toString(), { value -> value.toIntOrNull()?.let { onChange(exercise.sets, exercise.minRepetitions, it, exercise.restSeconds) } }, label = { Text("Repeticiones máximas") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(exercise.restSeconds.toString(), { value -> value.toIntOrNull()?.let { onChange(exercise.sets, exercise.minRepetitions, exercise.maxRepetitions, it) } }, label = { Text("Descanso (segundos)") }, modifier = Modifier.fillMaxWidth())
}
