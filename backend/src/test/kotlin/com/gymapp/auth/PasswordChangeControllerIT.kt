package com.gymapp.auth

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
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PasswordChangeControllerIT(@Autowired private val json: ObjectMapper, @LocalServerPort private val port: Int) {
    @Test fun `changes only the owner password and rejects the previous password`() {
        val email = "password-${UUID.randomUUID()}@example.com"
        val token = register(email, "Passw0rd!")
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), request("PUT", "/api/v1/me/password", token, mapOf("currentPassword" to "wrong", "newPassword" to "NewPass1!")).statusCode())
        assertEquals(HttpStatus.NO_CONTENT.value(), request("PUT", "/api/v1/me/password", token, mapOf("currentPassword" to "Passw0rd!", "newPassword" to "NewPass1!")).statusCode())
        assertEquals(HttpStatus.UNAUTHORIZED.value(), request("POST", "/api/v1/auth/login", null, mapOf("email" to email, "password" to "Passw0rd!")).statusCode())
        assertEquals(HttpStatus.OK.value(), request("POST", "/api/v1/auth/login", null, mapOf("email" to email, "password" to "NewPass1!")).statusCode())
    }
    private fun register(email: String, password: String) = body(request("POST", "/api/v1/auth/register", null, mapOf("email" to email, "password" to password, "acceptedTermsAt" to "2026-08-01T00:00:00Z")).body()).getValue("accessToken") as String
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> { val b=HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type","application/json"); if(token!=null)b.header("Authorization","Bearer $token"); val p=HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload ?: emptyMap<String,Any>())); return client.send(if(method=="PUT") b.PUT(p).build() else b.POST(p).build(),HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(value:String)=json.readValue(value,Map::class.java) as Map<String,Any>
    private companion object { val client=HttpClient.newHttpClient() }
}
