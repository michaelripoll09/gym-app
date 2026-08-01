package com.gymapp.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountDeletionControllerIT(
    @Autowired private val json: ObjectMapper,
    @Autowired private val jdbc: JdbcTemplate,
    @LocalServerPort private val port: Int,
) {
    @Test
    fun `deletes only the authenticated account and its private records`() {
        val owner = registerAccount()
        val other = registerAccount()
        val planId = UUID.randomUUID()
        jdbc.update("insert into training_profiles (id, user_id, experience_level, primary_profile, goal, availability_band, available_days_per_week, session_duration_minutes) values (?, ?, 'BEGINNER', 'FITNESS', 'MUSCLE_GAIN', 'LOW', 2, 45)", UUID.randomUUID(), owner.id)
        jdbc.update("insert into workout_plans (id, user_id, name) values (?, ?, 'Rutina privada')", planId, owner.id)
        jdbc.update("insert into workout_sessions (id, plan_id, user_id) values (?, ?, ?)", UUID.randomUUID(), planId, owner.id)
        assertEquals(HttpStatus.CREATED.value(), request("POST", "/api/v1/body-measurements", owner.token, mapOf("recordedOn" to LocalDate.now().minusDays(1).toString(), "weightKg" to 70.0)).statusCode())
        assertEquals(HttpStatus.CREATED.value(), request("POST", "/api/v1/progress-goals", owner.token, mapOf("type" to "BODY_WEIGHT", "targetValue" to 68.0)).statusCode())

        assertEquals(HttpStatus.NO_CONTENT.value(), request("DELETE", "/api/v1/me", owner.token, null).statusCode())

        assertEquals(HttpStatus.UNAUTHORIZED.value(), request("GET", "/api/v1/me", owner.token, null).statusCode())
        assertEquals(HttpStatus.OK.value(), request("GET", "/api/v1/me", other.token, null).statusCode())
        listOf("users" to "id", "consents" to "user_id", "training_profiles" to "user_id", "workout_plans" to "user_id", "workout_sessions" to "user_id", "body_measurements" to "user_id", "progress_goals" to "user_id").forEach { (table, userColumn) ->
            assertEquals(0, jdbc.queryForObject("select count(*) from $table where $userColumn=?", Int::class.java, owner.id), table)
        }
    }

    private fun registerAccount(): Account {
        val body = body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "delete-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-08-01T00:00:00Z")))
        return Account(UUID.fromString(body.getValue("userId") as String), body.getValue("accessToken") as String)
    }

    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        val body = HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload ?: emptyMap<String, Any>()))
        val request = when (method) { "GET" -> builder.GET().build(); "DELETE" -> builder.DELETE().build(); else -> builder.POST(body).build() }
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private data class Account(val id: UUID, val token: String)
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
