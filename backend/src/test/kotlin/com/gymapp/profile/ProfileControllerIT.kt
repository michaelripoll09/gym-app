package com.gymapp.profile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileControllerIT(@Autowired private val json: ObjectMapper, @LocalServerPort private val port: Int) {
    @Test
    fun `saves and returns an authenticated training profile`() {
        val token = registerToken()
        val request = validProfile()

        val saved = put(token, request)
        val retrieved = get(token)

        assertEquals(HttpStatus.OK.value(), saved.statusCode())
        assertEquals(HttpStatus.OK.value(), retrieved.statusCode())
        val profile = body(retrieved)
        assertEquals("BEGINNER", profile.getValue("experienceLevel"))
        assertEquals("CALISTHENICS", profile.getValue("primaryProfile"))
        assertEquals(listOf("RUNNING"), profile.getValue("secondaryProfiles"))
        assertEquals("MUSCLE_GAIN", profile.getValue("goal"))
        assertEquals("MEDIUM", profile.getValue("availabilityBand"))
        assertEquals(3, profile.getValue("availableDaysPerWeek"))
        assertEquals(60, profile.getValue("sessionDurationMinutes"))
    }

    @Test
    fun `returns not found when the authenticated account has no profile`() {
        val response = get(registerToken())

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode())
    }

    @Test
    fun `does not expose a profile to another authenticated account`() {
        val owner = registerToken()
        put(owner, validProfile())

        val response = get(registerToken())

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode())
    }

    @Test
    fun `rejects three secondary profiles`() {
        val response = put(registerToken(), validProfile(listOf("RUNNING", "CROSSFIT", "POWERLIFTING")))

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode())
    }

    private fun registerToken(): String {
        val response = post("/api/v1/auth/register", mapOf("email" to "profile-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-07-27T00:00:00Z"))
        return body(response).getValue("accessToken") as String
    }

    private fun validProfile(secondaries: List<String> = listOf("RUNNING")) = mapOf(
        "experienceLevel" to "BEGINNER", "primaryProfile" to "CALISTHENICS", "secondaryProfiles" to secondaries,
        "goal" to "MUSCLE_GAIN", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 3, "sessionDurationMinutes" to 60,
    )

    private fun post(path: String, payload: Any) = request("POST", path, null, payload)
    private fun put(token: String, payload: Any) = request("PUT", "/api/v1/me/training-profile", token, payload)
    private fun get(token: String) = request("GET", "/api/v1/me/training-profile", token)

    private fun request(method: String, path: String, token: String? = null, payload: Any? = null): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        val request = when (method) {
            "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build()
            "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build()
            else -> builder.GET().build()
        }
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    @Suppress("UNCHECKED_CAST")
    private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>

    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
