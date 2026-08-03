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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.GuidedRoutineProposalResponse

private val lime = Color(0xFFB9F227)
private val card = Color(0xFF1C2022)

@Composable
fun GuidedRoutineScreen(
    loading: Boolean,
    proposal: GuidedRoutineProposalResponse?,
    error: String?,
    saving: Boolean,
    onGenerate: () -> Unit,
    onConfirm: (GuidedRoutineProposalResponse) -> Unit,
    onDiscard: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Rutina guiada", color = lime, fontSize = 30.sp) }
        item { Text("Usaremos tu perfil y ejercicios publicados para preparar una propuesta inicial.", color = Color.LightGray) }
        when {
            loading -> item { Text("Preparando tu propuestaâ€¦", color = Color.LightGray) }
            error != null -> {
                item { Text(error, color = Color(0xFFFF8A80)) }
                item { Button(onClick = onGenerate) { Text("Reintentar") } }
            }
            proposal != null -> {
                item { Text(proposal.name, color = Color.White, fontSize = 21.sp) }
                item { Text(proposal.explanation, color = Color.LightGray) }
                items(proposal.days, key = { it.name }) { day ->
                    Card(colors = CardDefaults.cardColors(containerColor = card), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(day.name, color = Color.White, fontSize = 20.sp)
                            day.exercises.forEach { exercise ->
                                Text("${exercise.name} · ${exercise.sets}×${exercise.minRepetitions}-${exercise.maxRepetitions} · ${exercise.restSeconds}s", color = Color.LightGray)
                            }
                        }
                    }
                }
                item { Button(onClick = { onConfirm(proposal) }, enabled = !saving) { Text(if (saving) "Creando rutinaâ€¦" else "Crear esta rutina") } }
                item { Button(onClick = onDiscard, enabled = !saving) { Text("Descartar propuesta") } }
            }
            else -> item { Button(onClick = onGenerate) { Text("Generar propuesta") } }
        }
        item { Button(onClick = onBack, enabled = !saving) { Text("Volver al catálogo") } }
    }
}
