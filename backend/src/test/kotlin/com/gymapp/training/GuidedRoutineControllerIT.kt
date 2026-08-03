package com.gymapp.training

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuidedRoutineControllerIT(@Autowired private val json: ObjectMapper, @Autowired private val jdbc: JdbcTemplate, @LocalServerPort private val port: Int) {
    @Test fun `creates a compatible deterministic proposal without persisting a routine`() {
        seedGeneralFitnessExercise()
        val token = register()
        val profile = mapOf("experienceLevel" to "BEGINNER", "primaryProfile" to "GENERAL_FITNESS", "secondaryProfiles" to emptyList<String>(), "goal" to "Mejorar fuerza", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 2, "sessionDurationMinutes" to 45)
        assertEquals(200, request("PUT", "/api/v1/me/training-profile", token, profile).statusCode())

        val proposal = request("POST", "/api/v1/guided-routines/proposal", token, emptyMap<String, String>())
        assertEquals(200, proposal.statusCode())
        val body = body(proposal.body())
        assertEquals(2, (body["days"] as List<*>).size)
        assertTrue((body["explanation"] as String).isNotBlank())
        assertEquals("DETERMINISTIC_FALLBACK", body["source"])
        assertTrue(list(request("GET", "/api/v1/workout-plans", token, null).body()).isEmpty())
    }

    @Test fun `rejects a proposal when the user has not completed their profile`() {
        val response = request("POST", "/api/v1/guided-routines/proposal", register(), emptyMap<String, String>())

        assertEquals(422, response.statusCode())
    }

    @Test fun `rejects an incompatible replacement when the edited proposal is confirmed`() {
        seedIncompatibleExercise()
        val token = register()
        val profile = mapOf("experienceLevel" to "BEGINNER", "primaryProfile" to "GENERAL_FITNESS", "secondaryProfiles" to emptyList<String>(), "goal" to "MUSCLE_GAIN", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 2, "sessionDurationMinutes" to 45)
        request("PUT", "/api/v1/me/training-profile", token, profile)

        val response = request("POST", "/api/v1/workout-plans", token, mapOf(
            "name" to "Propuesta editada",
            "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf(
                "exerciseId" to "00000000-0000-0000-0000-000000000012", "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12, "restSeconds" to 90,
            )))),
        ))

        assertEquals(422, response.statusCode())
    }

    private fun register(): String { val response = request("POST", "/api/v1/auth/register", null, mapOf("email" to "guided-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-08-03T00:00:00Z")); return body(response.body()).getValue("accessToken") as String }
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> { val b = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json"); if (token != null) b.header("Authorization", "Bearer $token"); val request = when (method) { "GET" -> b.GET().build(); "PUT" -> b.PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build(); else -> b.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build() }; return client.send(request, HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(value: String) = json.readValue(value, Map::class.java) as Map<String, Any>
    @Suppress("UNCHECKED_CAST") private fun list(value: String) = json.readValue(value, List::class.java) as List<Any>
    private fun seedGeneralFitnessExercise() {
        val id = UUID.fromString("00000000-0000-0000-0000-000000000011")
        jdbc.update("insert into exercises (id, source_name, source_external_id, source_commit, name, spanish_instructions, published, source_file_sha256, attribution, review_status) values (?, 'guided-fixture', 'general-fitness', 'test', 'Sentadilla de prueba', 'Instrucción de prueba', true, '', null, 'APPROVED') on conflict (source_name, source_external_id) do nothing", id)
        jdbc.update("insert into exercise_training_profiles (exercise_id, profile_code) values (?, 'GENERAL_FITNESS') on conflict do nothing", id)
    }
    private fun seedIncompatibleExercise() {
        val id = UUID.fromString("00000000-0000-0000-0000-000000000012")
        jdbc.update("insert into exercises (id, source_name, source_external_id, source_commit, name, spanish_instructions, published, source_file_sha256, attribution, review_status) values (?, 'guided-fixture', 'incompatible', 'test', 'Curl incompatible', 'Instrucción de prueba', true, '', null, 'APPROVED') on conflict (source_name, source_external_id) do nothing", id)
        jdbc.update("insert into exercise_training_profiles (exercise_id, profile_code) values (?, 'BODYBUILDING') on conflict do nothing", id)
    }
    private companion object { val client = HttpClient.newHttpClient() }
}
