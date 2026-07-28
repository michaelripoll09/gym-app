package com.gymapp.training

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
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrainingControllerIT(@Autowired private val json: ObjectMapper, @Autowired private val jdbc: JdbcTemplate, @LocalServerPort private val port: Int) {
    @Test
    fun `creates a calisthenics plan and records two sets`() {
        val token = registerToken()
        saveCalisthenicsProfile(token)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id = e.id where p.profile_code = 'CALISTHENICS' limit 1", UUID::class.java)
        val created = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Base calistenia", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12))))))

        assertEquals(HttpStatus.CREATED.value(), created.statusCode())
        val planId = body(created).getValue("id") as String
        val session = request("POST", "/api/v1/workout-plans/$planId/sessions", token, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10), mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 9))))
        assertEquals(HttpStatus.CREATED.value(), session.statusCode())
    }

    @Test
    fun `rejects an exercise not tagged for the primary profile`() {
        val token = registerToken(); saveCalisthenicsProfile(token)
        val incompatible = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='BODYBUILDING' and not exists (select 1 from exercise_training_profiles cp where cp.exercise_id=e.id and cp.profile_code='CALISTHENICS') limit 1", UUID::class.java)
        val response = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Incompatible", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to incompatible.toString(), "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode())
    }

    @Test
    fun `rejects a session on another users plan`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Owner plan", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 1, "maxRepetitions" to 1))))))
        val other = registerToken()
        val response = request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 1))))
        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode())
    }

    private fun registerToken(): String = (body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "training-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-07-27T00:00:00Z"))).getValue("accessToken") as String)
    private fun saveCalisthenicsProfile(token: String) { request("PUT", "/api/v1/me/training-profile", token, mapOf("experienceLevel" to "BEGINNER", "primaryProfile" to "CALISTHENICS", "secondaryProfiles" to emptyList<String>(), "goal" to "MUSCLE_GAIN", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 3, "sessionDurationMinutes" to 60)) }
    private fun request(method: String, path: String, token: String?, payload: Any): HttpResponse<String> { val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json"); if (token != null) builder.header("Authorization", "Bearer $token"); return client.send(if (method == "PUT") builder.PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build() else builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build(), HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
