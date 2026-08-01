package com.gymapp.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymapp.network.ProgressGoalRequest
import com.gymapp.network.ProgressGoalResponse

@Composable
fun GoalsScreen(
    goals: List<ProgressGoalResponse>, loading: Boolean, error: String?,
    onSave: (ProgressGoalRequest, String?) -> Unit,
    onComplete: (ProgressGoalResponse) -> Unit, onDelete: (ProgressGoalResponse) -> Unit,
    onRetry: () -> Unit, onBack: () -> Unit
) {
    var type by remember { mutableStateOf("BODY_WEIGHT") }
    var value by remember { mutableStateOf("") }
    var exercise by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Mis objetivos", style = MaterialTheme.typography.headlineMedium) }
        item { Button(onClick = onBack) { Text("Volver a progreso") } }
        item { Button(onClick = { type = if (type == "BODY_WEIGHT") "EXERCISE_LOAD" else "BODY_WEIGHT" }) { Text(if (type == "BODY_WEIGHT") "Objetivo: peso corporal" else "Objetivo: carga por ejercicio") } }
        item { OutlinedTextField(value, { value = it }, label = { Text("Valor objetivo (kg)") }) }
        if (type == "EXERCISE_LOAD") item { OutlinedTextField(exercise, { exercise = it }, label = { Text("Nombre del ejercicio") }) }
        item { OutlinedTextField(date, { date = it }, label = { Text("Fecha opcional AAAA-MM-DD") }) }
        item {
            message?.let { Text(it) }
            Button(onClick = {
                val validationError = goalInputError(type, value, date, exercise)
                if (validationError != null) message = validationError else {
                    message = null
                    onSave(ProgressGoalRequest(type, value.toDouble(), date.ifBlank { null }, exercise.ifBlank { null }), editingId)
                }
            }) { Text(if (editingId == null) "Guardar objetivo" else "Guardar cambios") }
        }
        when {
            loading -> item { Text("Cargando objetivos...") }
            error != null -> item { Text(error); Button(onClick = onRetry) { Text("Reintentar") } }
            goals.isEmpty() -> item { Text("Aun no tienes objetivos activos.") }
            else -> items(goals, key = { it.id }) { goal ->
                Card { Column(Modifier.padding(16.dp)) {
                    Text(if (goal.type == "BODY_WEIGHT") "Peso corporal" else goal.exerciseName.orEmpty())
                    Text("Meta: ${goal.targetValue} kg - Actual: ${goal.currentValue ?: "sin datos"}")
                    val progress = goalProgress(goal)
                    Text("${progress.label} - Restante: ${progress.remainingValue} kg")
                    Button(onClick = { editingId = goal.id; type = goal.type; value = goal.targetValue.toString(); exercise = goal.exerciseName.orEmpty(); date = goal.targetDate.orEmpty() }) { Text("Editar") }
                    if (goal.status == "ACTIVE") Button(onClick = { onComplete(goal) }) { Text("Marcar completado") }
                    Button(onClick = { onDelete(goal) }) { Text("Eliminar") }
                } }
            }
        }
    }
}
