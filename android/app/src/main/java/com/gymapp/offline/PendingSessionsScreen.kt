package com.gymapp.offline

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

@Composable
fun PendingSessionsScreen(pending: List<PendingSession>, syncing: Boolean, message: String?, onSync: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Sesiones pendientes", color = Color(0xFFB9F227), fontSize = 30.sp) }
        item { Button(onClick = onBack) { Text("Volver a Mis rutinas") } }
        if (pending.isEmpty()) item { Text("Cola vacía. Todas tus sesiones están sincronizadas.", color = Color.LightGray) }
        if (pending.isNotEmpty()) item { Button(onClick = onSync, enabled = !syncing, modifier = Modifier.fillMaxWidth()) { Text(if (syncing) "Sincronizando…" else "Sincronizar ahora") } }
        message?.let { item { Text(it, color = if (it.startsWith("Error")) Color(0xFFFF8A80) else Color(0xFFB9F227)) } }
        items(pending, key = { it.localId }) { session ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2022)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(session.planName, color = Color.White, fontSize = 20.sp)
                    Text("Pendiente de sincronización", color = Color(0xFFFFD180))
                    Text("${session.request.sets.size} series · ${session.createdAt}", color = Color.LightGray)
                }
            }
        }
    }
}
