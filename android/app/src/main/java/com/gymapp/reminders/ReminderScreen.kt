package com.gymapp.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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

@Composable
fun ReminderScreen(settings: ReminderSettings, notificationPermissionGranted: Boolean, onSettingsChanged: (ReminderSettings) -> Unit, onRequestPermission: () -> Unit, onOpenSettings: () -> Unit, onBack: () -> Unit) {
    var hour by remember(settings.hour) { mutableStateOf(settings.hour.toString()) }
    var minute by remember(settings.minute) { mutableStateOf(settings.minute.toString()) }
    val parsedHour = hour.toIntOrNull()
    val parsedMinute = minute.toIntOrNull()
    val validTime = parsedHour in 0..23 && parsedMinute in 0..59
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Recordatorios", color = Color(0xFFB9F227), fontSize = 30.sp)
        Text("Te avisaremos en los días programados de tus rutinas activas.", color = Color.LightGray)
        Button(onClick = onBack) { Text("Volver a Mis rutinas") }
        OutlinedTextField(hour, { hour = it.filter(Char::isDigit).take(2) }, label = { Text("Hora (0-23)") }, modifier = Modifier.fillMaxWidth(), isError = hour.isNotEmpty() && !validTime)
        OutlinedTextField(minute, { minute = it.filter(Char::isDigit).take(2) }, label = { Text("Minuto (0-59)") }, modifier = Modifier.fillMaxWidth(), isError = minute.isNotEmpty() && !validTime)
        if (!validTime) Text("Indica una hora válida.", color = Color(0xFFFF8A80))
        if (!notificationPermissionGranted) {
            Text("Las notificaciones están desactivadas. Autorízalas para recibir recordatorios.", color = Color(0xFFFFD180))
            Button(onClick = onRequestPermission) { Text("Permitir notificaciones") }
            Button(onClick = onOpenSettings) { Text("Abrir ajustes de la app") }
        }
        Button(onClick = { if (validTime && notificationPermissionGranted) onSettingsChanged(ReminderSettings(true, parsedHour!!, parsedMinute!!)) }, enabled = validTime && notificationPermissionGranted) { Text(if (settings.enabled) "Actualizar recordatorios" else "Activar recordatorios") }
        if (settings.enabled) Button(onClick = { onSettingsChanged(settings.copy(enabled = false)) }) { Text("Desactivar recordatorios") }
        Text(if (settings.enabled) "Activos a las ${settings.hour.toString().padStart(2, '0')}:${settings.minute.toString().padStart(2, '0')}." else "No tienes recordatorios activos.", color = Color.LightGray)
    }
}
