package com.gymapp.curated

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
import com.gymapp.network.CuratedPlanResponse

private val curatedLime = Color(0xFFB9F227)
private val curatedCard = Color(0xFF1C2022)

@Composable
fun CuratedPlansScreen(
    state: CuratedPlansState,
    selected: CuratedPlanResponse?,
    adopting: Boolean,
    adoptionError: String?,
    onSelect: (CuratedPlanResponse) -> Unit,
    onAdopt: (CuratedPlanResponse) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    if (selected != null) {
        CuratedPlanDetail(selected, adopting, adoptionError, onAdopt, onBack)
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Planes recomendados", color = curatedLime, fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver al catálogo") } }
        when (state.content()) {
            CuratedPlansContent.LOADING -> item { Text("Buscando planes compatibles…", color = Color.LightGray) }
            CuratedPlansContent.ERROR -> {
                item { Text(state.error ?: "No pudimos cargar los planes", color = Color(0xFFFF8A80)) }
                item { Button(onClick = onRetry) { Text("Reintentar") } }
            }
            CuratedPlansContent.EMPTY -> item { Text("Aún no hay un plan curado compatible con tu perfil.", color = Color.LightGray) }
            CuratedPlansContent.READY -> items(state.plans, key = { it.id }) { plan ->
                Card(colors = CardDefaults.cardColors(containerColor = curatedCard), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(plan.name, color = Color.White, fontSize = 20.sp)
                        Text(plan.description, color = Color.LightGray)
                        Text("${plan.days.size} días por semana", color = curatedLime)
                        Button(onClick = { onSelect(plan) }) { Text("Ver programación") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedPlanDetail(
    plan: CuratedPlanResponse,
    adopting: Boolean,
    adoptionError: String?,
    onAdopt: (CuratedPlanResponse) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(plan.name, color = curatedLime, fontSize = 30.sp) }
        item { Text(plan.description, color = Color.LightGray) }
        item { Button(onClick = onBack) { Text("Volver a planes") } }
        items(plan.days, key = { it.name }) { day ->
            Card(colors = CardDefaults.cardColors(containerColor = curatedCard), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(day.name, color = Color.White, fontSize = 20.sp)
                    day.exercises.forEach { exercise ->
                        Text("${exercise.name} · ${exercise.sets}×${exercise.minRepetitions}-${exercise.maxRepetitions} · ${exercise.restSeconds}s", color = Color.LightGray)
                    }
                }
            }
        }
        item { adoptionError?.let { Text(it, color = Color(0xFFFF8A80)) } }
        item { Button(onClick = { onAdopt(plan) }, enabled = !adopting) { Text(if (adopting) "Creando rutina…" else "Usar esta rutina") } }
    }
}
