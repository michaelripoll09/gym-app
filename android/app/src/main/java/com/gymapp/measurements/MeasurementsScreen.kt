package com.gymapp.measurements

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.network.BodyMeasurementRequest
import com.gymapp.network.BodyMeasurementResponse
import java.time.LocalDate

private val measurementLime = Color(0xFFB9F227)
private val measurementCard = Color(0xFF1C2022)

@Composable
fun MeasurementsScreen(
    state: BodyMeasurementsState,
    saving: Boolean,
    editing: BodyMeasurementResponse?,
    message: String?,
    onSave: (BodyMeasurementRequest) -> Unit,
    onEdit: (BodyMeasurementResponse) -> Unit,
    onDelete: (BodyMeasurementResponse) -> Unit,
    onCancelEdit: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editing?.id) {
        date = editing?.recordedOn ?: LocalDate.now().toString()
        weight = editing?.weightKg?.toString().orEmpty()
        waist = editing?.waistCm?.toString().orEmpty()
        hip = editing?.hipCm?.toString().orEmpty()
        chest = editing?.chestCm?.toString().orEmpty()
        validationError = null
    }

    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Medidas corporales", color = measurementLime, fontSize = 30.sp) }
        item { Text("Tus datos son privados y solo están vinculados a tu cuenta.", color = Color.LightGray) }
        item { Button(onClick = onBack) { Text("Volver a progreso") } }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = measurementCard), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (editing == null) "Registrar medida" else "Editar medida", color = Color.White, fontSize = 20.sp)
                    Text("Fecha (AAAA-MM-DD)", color = Color.LightGray)
                    OutlinedTextField(date, { date = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Fecha") })
                    OutlinedTextField(weight, { weight = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Peso (kg) *") })
                    OutlinedTextField(waist, { waist = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Cintura (cm), opcional") })
                    OutlinedTextField(hip, { hip = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Cadera (cm), opcional") })
                    OutlinedTextField(chest, { chest = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Pecho (cm), opcional") })
                    (validationError ?: message)?.let { Text(it, color = Color(0xFFFF8A80)) }
                    Button(onClick = {
                        val parsedDate = runCatching { LocalDate.parse(date.trim()) }.getOrNull()
                        val parsedWeight = weight.trim().replace(',', '.').toDoubleOrNull()
                        fun optionalValue(raw: String) = raw.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                        val values = listOf(optionalValue(waist), optionalValue(hip), optionalValue(chest))
                        validationError = when {
                            parsedDate == null -> "Indica una fecha válida en formato AAAA-MM-DD."
                            parsedDate.isAfter(LocalDate.now()) -> "La fecha no puede estar en el futuro."
                            parsedWeight == null || parsedWeight !in 20.0..500.0 -> "El peso debe estar entre 20 y 500 kg."
                            listOf(waist, hip, chest).any { it.trim().isNotEmpty() } && values.any { it == null || it !in 20.0..250.0 } -> "Las medidas deben estar entre 20 y 250 cm."
                            else -> null
                        }
                        if (validationError == null) onSave(BodyMeasurementRequest(date.trim(), parsedWeight!!, values[0], values[1], values[2]))
                    }, enabled = !saving) { Text(if (saving) "Guardando…" else if (editing == null) "Guardar medida" else "Guardar cambios") }
                    if (editing != null) Button(onClick = onCancelEdit) { Text("Cancelar edición") }
                }
            }
        }
        when (state.content()) {
            MeasurementContent.LOADING -> item { Text("Cargando tus medidas…", color = Color.LightGray) }
            MeasurementContent.ERROR -> {
                item { Text(state.error ?: "No pudimos cargar tus medidas", color = Color(0xFFFF8A80)) }
                item { Button(onClick = onRetry) { Text("Reintentar") } }
            }
            MeasurementContent.EMPTY -> item { Text("Aún no tienes medidas registradas. Guarda la primera para ver tu tendencia.", color = Color.LightGray) }
            MeasurementContent.READY -> {
                item { Text("Tendencia", color = Color.White, fontSize = 22.sp) }
                items(measurementTrends(state.measurements), key = { it.label }) { trend ->
                    Card(colors = CardDefaults.cardColors(containerColor = measurementCard), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(trend.label, color = Color.White, fontSize = 18.sp)
                            Text("Último valor: ${formatValue(trend.latestValue)} ${trend.unit}", color = Color.LightGray)
                            Text("Cambio: ${formatChange(trend.change)} ${trend.unit}", color = measurementLime)
                        }
                    }
                }
                item { Text("Historial", color = Color.White, fontSize = 22.sp) }
                items(state.measurements, key = { it.id }) { measurement ->
                    Card(colors = CardDefaults.cardColors(containerColor = measurementCard), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(measurement.recordedOn, color = Color.White, fontSize = 18.sp)
                            Text(measurementDescription(measurement), color = Color.LightGray)
                            Button(onClick = { onEdit(measurement) }) { Text("Editar") }
                            Button(onClick = { onDelete(measurement) }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

private fun measurementDescription(measurement: BodyMeasurementResponse): String = buildList {
    add("Peso ${formatValue(measurement.weightKg)} kg")
    measurement.waistCm?.let { add("Cintura ${formatValue(it)} cm") }
    measurement.hipCm?.let { add("Cadera ${formatValue(it)} cm") }
    measurement.chestCm?.let { add("Pecho ${formatValue(it)} cm") }
}.joinToString(" · ")

private fun formatValue(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(java.util.Locale.US, "%.1f", value)
private fun formatChange(value: Double): String = if (value > 0) "+${formatValue(value)}" else formatValue(value)
