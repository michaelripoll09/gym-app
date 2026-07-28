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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SessionScreen(state: SessionDraftState, saving: Boolean, error: String?, onStateChanged: (SessionDraftState) -> Unit, onFinish: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Entrenar · ${state.planName}", color = Color(0xFFB9F227), fontSize = 28.sp) }
        item { Button(onClick = onBack, enabled = !saving) { Text("Cancelar") } }
        itemsIndexed(state.sets, key = { index, set -> "${set.exerciseId}-$index" }) { index, set ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${set.exerciseName} · Serie ${set.setNumber}", color = Color.White)
                    OutlinedTextField(set.repetitions, { onStateChanged(state.updateRepetitions(index, it)) }, label = { Text("Repeticiones realizadas") }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                }
            }
        }
        state.validationMessage()?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = onFinish, enabled = canFinishSession(saving), modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando sesión…" else "Finalizar sesión") } }
    }
}
