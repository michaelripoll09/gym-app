package com.gymapp.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklySummaryScreen(state: WeeklySummaryState, onRetry: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Resumen semanal", color = Color(0xFFB9F227), fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver") } }
        when (state.content()) {
            WeeklySummaryContent.LOADING -> item { Text("Calculando tu semana…", color = Color.LightGray) }
            WeeklySummaryContent.ERROR -> {
                item { Text(state.error ?: "No pudimos cargar el resumen", color = Color(0xFFFF8A80)) }
                item { Button(onClick = onRetry) { Text("Reintentar") } }
            }
            WeeklySummaryContent.EMPTY -> item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tu semana está lista para comenzar", color = Color.White, fontSize = 20.sp)
                        Text("Crea o adopta una rutina y completa una sesión para ver tu adherencia y volumen.", color = Color.LightGray)
                    }
                }
            }
            WeeklySummaryContent.READY -> item {
                val summary = state.summary!!
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Esta semana", color = Color.White, fontSize = 20.sp)
                        Text("${summary.completedSessions} de ${summary.scheduledSessions} sesiones realizadas", color = Color.LightGray)
                        Text("Adherencia: ${summary.adherencePercent}%", color = Color(0xFFB9F227), fontSize = 18.sp)
                        Text("Volumen: ${summary.volumeKg.formatKg()} kg", color = Color.LightGray)
                        summary.nextSession?.let { Text("Próxima sesión: ${it.planName} · ${it.dayName}", color = Color.White) }
                    }
                }
            }
        }
    }
}

private fun Double.formatKg() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
