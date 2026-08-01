package com.gymapp.offline

import com.gymapp.network.CreateWorkoutSessionRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PendingSession(
    val localId: String,
    val planId: String,
    val planName: String,
    val createdAt: String,
    val request: CreateWorkoutSessionRequest,
)

fun enqueuePendingSession(queue: List<PendingSession>, session: PendingSession): List<PendingSession> =
    if (queue.any { it.localId == session.localId }) queue else queue + session

fun removeSyncedSession(queue: List<PendingSession>, localId: String): List<PendingSession> = queue.filterNot { it.localId == localId }

fun keepPendingAfterFailure(session: PendingSession): PendingSession = session

object OfflineSessionCodec {
    private val json = Json { ignoreUnknownKeys = true }
    fun encode(sessions: List<PendingSession>): String = json.encodeToString(sessions)
    fun decode(value: String): List<PendingSession> = runCatching { json.decodeFromString<List<PendingSession>>(value) }.getOrDefault(emptyList())
}
