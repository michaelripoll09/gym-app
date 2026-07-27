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
