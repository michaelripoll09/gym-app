package com.gymapp.progress

import com.gymapp.network.ProgressAnalysisResponse

enum class ProgressAnalysisContent { LOADING, ERROR, EMPTY, READY }

data class ProgressAnalysisState(
    val analysis: ProgressAnalysisResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content() = when {
        loading -> ProgressAnalysisContent.LOADING
        error != null -> ProgressAnalysisContent.ERROR
        analysis?.sufficientData == true -> ProgressAnalysisContent.READY
        else -> ProgressAnalysisContent.EMPTY
    }

    fun emptyMessage() = "Registra sesiones, medidas u objetivos para recibir un análisis de progreso."
}

fun formatWeightChange(value: Double): String {
    val normalized = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    return "$normalized kg"
}
