package com.gymapp.summary

import com.gymapp.network.WeeklyTrainingSummaryResponse

enum class WeeklySummaryContent { LOADING, ERROR, EMPTY, READY }

data class WeeklySummaryState(
    val summary: WeeklyTrainingSummaryResponse? = null,
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content(): WeeklySummaryContent = when {
        loading -> WeeklySummaryContent.LOADING
        error != null -> WeeklySummaryContent.ERROR
        summary == null || (summary.completedSessions == 0 && summary.scheduledSessions == 0) -> WeeklySummaryContent.EMPTY
        else -> WeeklySummaryContent.READY
    }
}
