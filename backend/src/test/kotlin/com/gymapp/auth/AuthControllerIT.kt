package com.gymapp.auth

import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["gym-app.auth.jwt-secret=Z3ltLWFwcC1kZXZlbG9wbWVudC1rZXktMzItYnl0ZXMtbG9uZw=="],
)
class AuthControllerIT(
    @Autowired private val objectMapper: ObjectMapper,
    @LocalServerPort private val port: Int,
) {
    @Test
    fun `register returns a user id and an access token`() {
        val response = post("/api/v1/auth/register", registrationRequest())

        assertEquals(HttpStatus.CREATED.value(), response.statusCode())
        assertContainsAuthResponse(response)
    }

    @Test
    fun `register rejects a duplicated email`() {
        val request = registrationRequest()
        post("/api/v1/auth/register", request)

        val response = post("/api/v1/auth/register", request)

        assertEquals(HttpStatus.CONFLICT.value(), response.statusCode())
    }

    @Test
    fun `login returns a new access token for valid credentials`() {
        val request = registrationRequest()
        post("/api/v1/auth/register", request)

        val response = post("/api/v1/auth/login", mapOf("email" to request.getValue("email"), "password" to request.getValue("password")))

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertContainsAuthResponse(response)
    }

    @Test
    fun `me rejects requests without a bearer token`() {
        val response = get("/api/v1/me")

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode())
    }

    @Test
    fun `me returns the account identified by a bearer token`() {
        val request = registrationRequest()
        val registration = post("/api/v1/auth/register", request)
        val token = responseBody(registration).getValue("accessToken") as String

        val response = get("/api/v1/me", token)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals(request.getValue("email"), responseBody(response).getValue("email"))
    }

    private fun registrationRequest(): Map<String, String> = mapOf(
        "email" to "ana-${UUID.randomUUID()}@example.com",
        "password" to "Passw0rd!",
        "acceptedTermsAt" to "2026-07-27T00:00:00Z",
    )

    private fun post(path: String, body: Map<String, String>): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    private fun get(path: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI.create(url(path))).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    private fun get(path: String, bearerToken: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI.create(url(path)))
                .header("Authorization", "Bearer $bearerToken")
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    private fun assertContainsAuthResponse(response: HttpResponse<String>) {
        val body = responseBody(response)
        assertNotNull(body["userId"])
        assertTrue(body["accessToken"] is String)
    }

    @Suppress("UNCHECKED_CAST")
    private fun responseBody(response: HttpResponse<String>): Map<String, Any> =
        objectMapper.readValue(response.body(), Map::class.java) as Map<String, Any>

    private fun url(path: String) = "http://localhost:$port$path"

    private companion object {
        val client: HttpClient = HttpClient.newHttpClient()
    }
}
