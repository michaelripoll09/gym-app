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

data class ExerciseLoadProgress(val exerciseName: String, val latestLoadKg: Double, val maximumLoadKg: Double)

fun exerciseLoadProgress(sessions: List<WorkoutSessionResponse>): List<ExerciseLoadProgress> = sessions
    .flatMap { session -> session.sets.mapNotNull { set -> set.loadKg?.let { Triple(session, set.exerciseName, it) } } }
    .groupBy { it.second }
    .map { (name, entries) ->
        val recent = entries.maxBy { runCatching { parseStartedAt(it.first.startedAt) }.getOrDefault(Instant.EPOCH) }
        ExerciseLoadProgress(name, recent.third, entries.maxOf { it.third }) to
            runCatching { parseStartedAt(recent.first.startedAt) }.getOrDefault(Instant.EPOCH)
    }
    .sortedWith(compareByDescending<Pair<ExerciseLoadProgress, Instant>> { it.second }.thenBy { it.first.exerciseName })
    .map { it.first }

data class TrainingProgressState(
    val recentSessions: List<WorkoutSessionResponse> = emptyList(),
    val summary: ProgressSummary = ProgressSummary(),
    val exerciseLoads: List<ExerciseLoadProgress> = emptyList(),
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
                exerciseLoads = exerciseLoadProgress(sessions),
            )
        }
    }
}

private fun parseStartedAt(value: String): Instant = runCatching { Instant.parse(value) }.getOrElse {
    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).toInstant(ZoneOffset.UTC)
}
