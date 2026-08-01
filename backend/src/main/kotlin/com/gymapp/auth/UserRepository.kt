package com.gymapp.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
}

interface ConsentRepository : JpaRepository<Consent, UUID>

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenHash(tokenHash: String): PasswordResetToken?
}
