package com.gymapp.progress

import com.gymapp.network.WorkoutSessionResponse
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class ProgressContent { LOADING, ERROR, EMPTY, READY }

data class ProgressSummary(
    val completedSessions: Int = 0,
    val registeredSets: Int = 0,
    val totalRepetitions: Int = 0,
)

data class TrainingProgressState(
    val recentSessions: List<WorkoutSessionResponse> = emptyList(),
    val summary: ProgressSummary = ProgressSummary(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content(): ProgressContent = when {
        loading -> ProgressContent.LOADING
        error != null -> ProgressContent.ERROR
        recentSessions.isEmpty() -> ProgressContent.EMPTY
        else -> ProgressContent.READY
    }

    companion object {
        fun loaded(sessions: List<WorkoutSessionResponse>, now: Instant): TrainingProgressState {
            val recent = sessions.filter { session ->
                runCatching { Duration.between(parseStartedAt(session.startedAt), now).toDays() in 0..6 }.getOrDefault(false)
            }
            return TrainingProgressState(
                recentSessions = sessions,
                summary = ProgressSummary(
                    completedSessions = recent.size,
                    registeredSets = recent.sumOf { it.sets.size },
                    totalRepetitions = recent.sumOf { session -> session.sets.sumOf { it.repetitions } },
                ),
            )
        }
    }
}

private fun parseStartedAt(value: String): Instant = runCatching { Instant.parse(value) }.getOrElse {
    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).toInstant(ZoneOffset.UTC)
}
