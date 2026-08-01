package com.gymapp.auth

import com.gymapp.GymAppApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.security.MessageDigest
import java.time.Instant
import java.sql.Timestamp

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [GymAppApplication::class, PasswordResetControllerIT.TestConfig::class])
@Import(PasswordResetControllerIT.TestConfig::class)
class PasswordResetControllerIT(@Autowired private val json: ObjectMapper, @Autowired private val mailer: CapturingPasswordResetMailer, @Autowired private val jdbc: JdbcTemplate, @LocalServerPort private val port: Int) {
    @Test fun `request is neutral and a valid mailed token resets the password only once`() {
        val email = "reset-${UUID.randomUUID()}@example.com"
        register(email, "Passw0rd!")
        assertEquals(202, post("/api/v1/auth/password-resets", mapOf("email" to email)).statusCode())
        assertEquals(202, post("/api/v1/auth/password-resets", mapOf("email" to "missing-${UUID.randomUUID()}@example.com")).statusCode())
        assertEquals(204, post("/api/v1/auth/password-resets/confirm", mapOf("token" to mailer.token, "newPassword" to "NewPass1!")).statusCode())
        assertEquals(422, post("/api/v1/auth/password-resets/confirm", mapOf("token" to mailer.token, "newPassword" to "Another1!")).statusCode())
        assertEquals(401, post("/api/v1/auth/login", mapOf("email" to email, "password" to "Passw0rd!")).statusCode())
        assertEquals(200, post("/api/v1/auth/login", mapOf("email" to email, "password" to "NewPass1!")).statusCode())
    }

    @Test fun `rejects an expired reset token`() {
        val email = "expired-${UUID.randomUUID()}@example.com"
        register(email, "Passw0rd!")
        post("/api/v1/auth/password-resets", mapOf("email" to email))
        jdbc.update("update password_reset_tokens set expires_at=? where token_hash=?", Timestamp.from(Instant.now().minusSeconds(1)), hash(mailer.token))

        assertEquals(422, post("/api/v1/auth/password-resets/confirm", mapOf("token" to mailer.token, "newPassword" to "NewPass1!")).statusCode())
    }

    private fun register(email: String, password: String) = post("/api/v1/auth/register", mapOf("email" to email, "password" to password, "acceptedTermsAt" to "2026-08-01T00:00:00Z"))
    private fun post(path: String, payload: Any) = client.send(HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build(), HttpResponse.BodyHandlers.ofString())
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    @TestConfiguration class TestConfig {
        @Bean @Primary fun passwordResetMailer() = CapturingPasswordResetMailer()
    }
    class CapturingPasswordResetMailer : PasswordResetMailer {
        var token = ""
        override fun send(email: String, token: String) { this.token = token }
    }
    private companion object { val client = HttpClient.newHttpClient() }
}
