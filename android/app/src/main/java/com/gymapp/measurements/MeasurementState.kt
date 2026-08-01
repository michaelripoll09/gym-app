package com.gymapp.measurements

import com.gymapp.network.BodyMeasurementResponse

data class MeasurementTrend(val label: String, val unit: String, val latestValue: Double, val change: Double)

data class BodyMeasurementsState(
    val measurements: List<BodyMeasurementResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content() = when {
        loading -> MeasurementContent.LOADING
        error != null -> MeasurementContent.ERROR
        measurements.isEmpty() -> MeasurementContent.EMPTY
        else -> MeasurementContent.READY
    }
}

enum class MeasurementContent { LOADING, ERROR, EMPTY, READY }

fun measurementTrends(measurements: List<BodyMeasurementResponse>): List<MeasurementTrend> = listOfNotNull(
    trendFor("Peso", "kg", measurements) { it.weightKg },
    trendFor("Cintura", "cm", measurements) { it.waistCm },
    trendFor("Cadera", "cm", measurements) { it.hipCm },
    trendFor("Pecho", "cm", measurements) { it.chestCm },
)

private fun trendFor(label: String, unit: String, measurements: List<BodyMeasurementResponse>, value: (BodyMeasurementResponse) -> Double?): MeasurementTrend? {
    val recorded = measurements.sortedBy { it.recordedOn }.mapNotNull { measurement -> value(measurement)?.let { measurement to it } }
    if (recorded.isEmpty()) return null
    val first = recorded.first().second
    val latest = recorded.last().second
    return MeasurementTrend(label, unit, latest, latest - first)
}

fun replaceMeasurement(measurements: List<BodyMeasurementResponse>, updated: BodyMeasurementResponse): List<BodyMeasurementResponse> =
    measurements.map { if (it.id == updated.id) updated else it }.sortedByDescending { it.recordedOn }

fun removeMeasurement(measurements: List<BodyMeasurementResponse>, id: String): List<BodyMeasurementResponse> =
    measurements.filterNot { it.id == id }
