package com.gymapp.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gymapp.network.CalendarDayResponse
import java.time.YearMonth

@Composable
fun TrainingCalendarScreen(
    month: YearMonth,
    state: TrainingCalendarState,
    onMonth: (YearMonth) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf<CalendarDayResponse?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Calendario de adherencia", color = Color(0xFFB9F227))
            Row {
                Button(onClick = { onMonth(calendarMonthBefore(month)) }) { Text("Anterior") }
                Text(month.toString(), Modifier.padding(16.dp))
                Button(onClick = { onMonth(calendarMonthAfter(month)) }) { Text("Siguiente") }
            }
            Button(onClick = onBack) { Text("Volver") }
        }
        selected?.let { day ->
            item {
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text(day.planName ?: "Sesion completada")
                        Text("${day.setCount} series registradas")
                    }
                }
            }
        }
        if (state.loading) item { Text("Cargando calendario...") }
        state.error?.let { error ->
            item {
                Text(error, color = Color(0xFFFF8A80))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }
        if (!state.loading && state.days.isEmpty() && state.error == null) {
            item { Text("No hay sesiones ni dias programados este mes.", color = Color.LightGray) }
        }
        items(state.days, key = { it.date }) { day ->
            Card(onClick = { if (day.completed) selected = day }) {
                Text(
                    "${day.date}: ${if (day.completed) "Completado" else if (day.scheduled) "Programado" else "Sin entrenamiento"}",
                    Modifier.padding(12.dp),
                    color = if (day.completed) Color(0xFFB9F227) else Color.White,
                )
            }
        }
    }
}
