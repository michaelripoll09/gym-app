package com.gymapp.auth

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import java.util.UUID

@Service
class AuthService(
    private val users: UserRepository,
    private val consents: ConsentRepository,
    private val jwt: JwtService,
    private val jdbc: JdbcTemplate,
    private val passwordResetTokens: PasswordResetTokenRepository,
    private val passwordResetMailer: PasswordResetMailer,
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = normalizeEmail(request.email)
        if (users.findByEmail(email) != null) throw DuplicateEmailException()

        val passwordHash = requireNotNull(passwordEncoder.encode(request.password))
        val user = users.save(User(UUID.randomUUID(), email, passwordHash, Instant.now()))
        consents.save(Consent(UUID.randomUUID(), user.id, "TERMS", request.acceptedTermsAt))
        return AuthResponse(user.id, jwt.issue(user.id))
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = users.findByEmail(normalizeEmail(request.email)) ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) throw InvalidCredentialsException()
        return AuthResponse(user.id, jwt.issue(user.id))
    }

    fun me(userId: UUID): MeResponse {
        val user = users.findById(userId).orElseThrow { InvalidCredentialsException() }
        return MeResponse(user.id, user.email)
    }

    @Transactional
    fun deleteAccount(userId: UUID) {
        if (!users.existsById(userId)) throw InvalidCredentialsException()
        jdbc.update("delete from workout_plans where user_id=?", userId)
        jdbc.update("delete from training_profiles where user_id=?", userId)
        jdbc.update("delete from consents where user_id=?", userId)
        users.deleteById(userId)
    }

    @Transactional fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        val user = users.findById(userId).orElseThrow { InvalidCredentialsException() }
        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash) || request.newPassword.length < 8) throw InvalidPasswordChangeException()
        jdbc.update("update users set password_hash=? where id=?", requireNotNull(passwordEncoder.encode(request.newPassword)), userId)
    }

    @Transactional
    fun requestPasswordReset(request: PasswordResetRequest) {
        val user = users.findByEmail(normalizeEmail(request.email)) ?: return
        val token = newResetToken()
        passwordResetTokens.save(PasswordResetToken(UUID.randomUUID(), user.id, hash(token), Instant.now().plusSeconds(900), createdAt = Instant.now()))
        runCatching { passwordResetMailer.send(user.email, token) }
    }

    @Transactional
    fun confirmPasswordReset(request: PasswordResetConfirmationRequest) {
        if (request.newPassword.length < 8) throw InvalidPasswordResetException()
        val token = passwordResetTokens.findByTokenHash(hash(request.token)) ?: throw InvalidPasswordResetException()
        if (token.usedAt != null || token.expiresAt.isBefore(Instant.now())) throw InvalidPasswordResetException()
        val user = users.findById(token.userId).orElseThrow { InvalidPasswordResetException() }
        jdbc.update("update users set password_hash=? where id=?", requireNotNull(passwordEncoder.encode(request.newPassword)), user.id)
        token.usedAt = Instant.now()
        passwordResetTokens.save(token)
    }

    private fun normalizeEmail(value: String) = value.trim().lowercase(Locale.ROOT)
    private fun newResetToken() = ByteArray(32).also(SecureRandom()::nextBytes).let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}

class DuplicateEmailException : RuntimeException()
class InvalidCredentialsException : RuntimeException()
class InvalidPasswordChangeException : RuntimeException()
class InvalidPasswordResetException : RuntimeException()
