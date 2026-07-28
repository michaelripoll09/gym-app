package com.gymapp.catalog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExerciseControllerIT(@LocalServerPort private val port: Int) {
    @Test
    fun `lists published exercises for a profile`() {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder(URI.create("http://localhost:$port/api/v1/exercises?profile=CALISTHENICS")).GET().build(),
            HttpResponse.BodyHandlers.ofString(),
        )

        assertEquals(HttpStatus.OK.value(), response.statusCode())
    }
}
