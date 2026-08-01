package com.gymapp.measurements

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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MeasurementsControllerIT(@Autowired private val json: ObjectMapper, @LocalServerPort private val port: Int) {
    @Test
    fun `creates updates deletes and isolates dated body measurements`() {
        val owner = registerToken()
        val recordedOn = LocalDate.now().minusDays(1).toString()
        val created = request("POST", "/api/v1/body-measurements", owner, mapOf("recordedOn" to recordedOn, "weightKg" to 76.4, "waistCm" to 82.0, "hipCm" to 96.0))

        assertEquals(HttpStatus.CREATED.value(), created.statusCode())
        val measurement = body(created)
        assertEquals(recordedOn, measurement.getValue("recordedOn"))
        assertEquals(76.4, measurement.getValue("weightKg"))
        assertEquals(82.0, measurement.getValue("waistCm"))
        assertEquals(null, measurement["chestCm"])

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), request("POST", "/api/v1/body-measurements", owner, mapOf("recordedOn" to recordedOn, "weightKg" to 70.0)).statusCode())
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), request("POST", "/api/v1/body-measurements", owner, mapOf("recordedOn" to LocalDate.now().toString(), "weightKg" to 900.0)).statusCode())

        val other = registerToken()
        assertEquals(emptyList<Any>(), json.readValue(request("GET", "/api/v1/body-measurements", other, null).body(), List::class.java))
        assertEquals(HttpStatus.FORBIDDEN.value(), request("PUT", "/api/v1/body-measurements/${measurement.getValue("id")}", other, mapOf("recordedOn" to recordedOn, "weightKg" to 72.0)).statusCode())

        assertEquals(HttpStatus.NO_CONTENT.value(), request("PUT", "/api/v1/body-measurements/${measurement.getValue("id")}", owner, mapOf("recordedOn" to recordedOn, "weightKg" to 75.2, "chestCm" to 101.0)).statusCode())
        val listed = json.readValue(request("GET", "/api/v1/body-measurements", owner, null).body(), List::class.java) as List<Map<String, Any?>>
        assertEquals(1, listed.size)
        assertEquals(75.2, listed.single().getValue("weightKg"))
        assertEquals(101.0, listed.single().getValue("chestCm"))

        assertEquals(HttpStatus.NO_CONTENT.value(), request("DELETE", "/api/v1/body-measurements/${measurement.getValue("id")}", owner, null).statusCode())
        assertTrue((json.readValue(request("GET", "/api/v1/body-measurements", owner, null).body(), List::class.java) as List<*>).isEmpty())
    }

    private fun registerToken(): String = body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "measurements-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-08-01T00:00:00Z"))).getValue("accessToken") as String
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        val body = HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload ?: emptyMap<String, Any>()))
        val request = when (method) { "GET" -> builder.GET().build(); "PUT" -> builder.PUT(body).build(); "DELETE" -> builder.DELETE().build(); else -> builder.POST(body).build() }
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }
    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
