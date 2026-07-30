package com.gymapp.sessions

import com.gymapp.network.WorkoutSessionResponse

enum class HistoryContent { LOADING, ERROR, EMPTY, LIST, DETAIL }

data class SessionHistoryState(
    val sessions: List<WorkoutSessionResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val selected: WorkoutSessionResponse? = null
) {
    fun content() = when {
        loading -> HistoryContent.LOADING
        error != null -> HistoryContent.ERROR
        selected != null -> HistoryContent.DETAIL
        sessions.isEmpty() -> HistoryContent.EMPTY
        else -> HistoryContent.LIST
    }

    fun select(session: WorkoutSessionResponse) = copy(selected = session)
}
