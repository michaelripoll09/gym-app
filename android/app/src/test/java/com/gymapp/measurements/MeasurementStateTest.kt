package com.gymapp.measurements

import com.gymapp.network.BodyMeasurementResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementStateTest {
    private val oldest = BodyMeasurementResponse("one", "2026-07-01", 80.0, 90.0, null, 102.0)
    private val newest = BodyMeasurementResponse("two", "2026-08-01", 78.5, 86.0, null, 100.0)

    @Test fun `shows latest values and change for every recorded metric`() {
        val trends = measurementTrends(listOf(newest, oldest))

        assertEquals(78.5, trends.first { it.label == "Peso" }.latestValue, 0.0)
        assertEquals(-1.5, trends.first { it.label == "Peso" }.change, 0.0)
        assertEquals(-4.0, trends.first { it.label == "Cintura" }.change, 0.0)
        assertEquals(-2.0, trends.first { it.label == "Pecho" }.change, 0.0)
        assertEquals(null, trends.firstOrNull { it.label == "Cadera" })
    }

    @Test fun `keeps an edited measurement and removes only the selected one`() {
        val edited = newest.copy(weightKg = 78.0)
        assertEquals(listOf(edited, oldest), replaceMeasurement(listOf(newest, oldest), edited))
        assertEquals(listOf(oldest), removeMeasurement(listOf(newest, oldest), newest.id))
    }
}
