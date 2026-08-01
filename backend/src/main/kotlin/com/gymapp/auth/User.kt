package com.gymapp.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    val id: UUID,
    @Column(nullable = false, unique = true, length = 320)
    val email: String,
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)

@Entity
@Table(name = "consents")
class Consent(
    @Id
    val id: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "consent_type", nullable = false)
    val consentType: String,
    @Column(name = "accepted_at", nullable = false)
    val acceptedAt: Instant,
)

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) val tokenHash: String,
    @Column(name = "expires_at", nullable = false) val expiresAt: Instant,
    @Column(name = "used_at") var usedAt: Instant? = null,
    @Column(name = "created_at", nullable = false) val createdAt: Instant,
)
