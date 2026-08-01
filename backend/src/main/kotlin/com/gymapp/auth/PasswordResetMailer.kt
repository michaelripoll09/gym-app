package com.gymapp.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

interface PasswordResetMailer {
    fun send(email: String, token: String)
}

@Service
class ResendPasswordResetMailer(
    @Value("\${gym-app.email.resend-api-key:}") private val apiKey: String,
    @Value("\${gym-app.email.from:onboarding@resend.dev}") private val from: String,
    @Value("\${gym-app.email.reset-url:gymapp://reset-password}") private val resetUrl: String,
) : PasswordResetMailer {
    private val client = HttpClient.newHttpClient()

    override fun send(email: String, token: String) {
        if (apiKey.isBlank()) return
        val link = "$resetUrl?token=${URLEncoder.encode(token, StandardCharsets.UTF_8)}"
        val json = """{"from":"${escape(from)}","to":["${escape(email)}"],"subject":"Restablece tu contraseña de Gym App","html":"<p>Solicitaste restablecer tu contraseña.</p><p><a href=\"$link\">Crear una nueva contraseña</a></p><p>Este enlace vence en 15 minutos.</p>"}"""
        val request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
            .header("Authorization", "Bearer $apiKey").header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.discarding())
        check(response.statusCode() in 200..299)
    }

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
}
