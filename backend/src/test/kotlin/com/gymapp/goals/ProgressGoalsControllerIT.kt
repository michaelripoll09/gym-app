package com.gymapp.goals

import org.junit.jupiter.api.Assertions.assertEquals
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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProgressGoalsControllerIT(@Autowired private val json: ObjectMapper, @LocalServerPort private val port: Int) {
    @Test fun `creates completes deletes and isolates private body weight goals`() {
        val owner = token(); val future = LocalDate.now().plusDays(30).toString()
        val created = request("POST", "/api/v1/progress-goals", owner, mapOf("type" to "BODY_WEIGHT", "targetValue" to 70.0, "targetDate" to future))
        assertEquals(HttpStatus.CREATED.value(), created.statusCode())
        val goal = body(created); assertEquals("BODY_WEIGHT", goal.getValue("type")); assertEquals(70.0, goal.getValue("targetValue")); assertEquals("ACTIVE", goal.getValue("status"))
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), request("POST", "/api/v1/progress-goals", owner, mapOf("type" to "BODY_WEIGHT", "targetValue" to -1)).statusCode())
        assertEquals(HttpStatus.NO_CONTENT.value(), request("PUT", "/api/v1/progress-goals/${goal.getValue("id")}", owner, mapOf("type" to "BODY_WEIGHT", "targetValue" to 72.0, "targetDate" to future)).statusCode())
        val other = token(); assertEquals(emptyList<Any>(), json.readValue(request("GET", "/api/v1/progress-goals", other, null).body(), List::class.java)); assertEquals(HttpStatus.FORBIDDEN.value(), request("PUT", "/api/v1/progress-goals/${goal.getValue("id")}/complete", other, null).statusCode())
        assertEquals(HttpStatus.NO_CONTENT.value(), request("PUT", "/api/v1/progress-goals/${goal.getValue("id")}/complete", owner, null).statusCode())
        val listed = json.readValue(request("GET", "/api/v1/progress-goals", owner, null).body(), List::class.java) as List<Map<String, Any>>
        assertEquals("COMPLETED", listed.single().getValue("status"))
        assertEquals(HttpStatus.NO_CONTENT.value(), request("DELETE", "/api/v1/progress-goals/${goal.getValue("id")}", owner, null).statusCode())
    }
    private fun token() = body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "goals-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-08-01T00:00:00Z"))).getValue("accessToken") as String
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> { val b = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json"); if (token != null) b.header("Authorization", "Bearer $token"); val p = HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload ?: emptyMap<String, Any>())); return client.send(when(method) { "GET" -> b.GET().build(); "PUT" -> b.PUT(p).build(); "DELETE" -> b.DELETE().build(); else -> b.POST(p).build() }, HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
