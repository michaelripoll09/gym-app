package com.gymapp.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(@Value("\${gym-app.auth.jwt-secret}") encodedSecret: String) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret))

    fun issue(userId: UUID): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(3600)))
            .signWith(signingKey)
            .compact()
    }

    fun subject(token: String): UUID? = runCatching {
        UUID.fromString(Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload.subject)
    }.getOrNull()
}
