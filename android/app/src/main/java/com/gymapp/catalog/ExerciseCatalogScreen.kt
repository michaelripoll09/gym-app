package com.gymapp.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExerciseCatalogScreen(state: ExerciseCatalogState, onCreateRoutine: () -> Unit, onShowRoutines: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Tus ejercicios", color = Color(0xFFB9F227), fontSize = 30.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onCreateRoutine) { Text("Crear rutina") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onShowRoutines) { Text("Mis rutinas") }
        }
        if (state.loading) item { Text("Cargando catálogo…", color = Color.LightGray) }
        state.error?.let { item { Text(it, color = Color.Red) } }
        items(state.exercises) { exercise ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022))) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(exercise.name, color = Color.White, fontSize = 18.sp)
                    Text(exercise.spanishInstructions, color = Color.LightGray)
                }
            }
        }
    }
}
