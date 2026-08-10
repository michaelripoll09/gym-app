package com.gymapp.sessions

data class SessionMutationRefreshState(
    val history: Int,
    val progress: Int,
    val calendar: Int,
    val weeklySummary: Int,
)

fun refreshAfterSessionMutation(state: SessionMutationRefreshState) = state.copy(
    history = state.history + 1,
    progress = state.progress + 1,
    calendar = state.calendar + 1,
    weeklySummary = state.weeklySummary + 1,
)
