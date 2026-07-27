package com.gymapp.auth

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Locale
import java.util.UUID

@Service
class AuthService(
    private val users: UserRepository,
    private val consents: ConsentRepository,
    private val jwt: JwtService,
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

    private fun normalizeEmail(value: String) = value.trim().lowercase(Locale.ROOT)
}

class DuplicateEmailException : RuntimeException()
class InvalidCredentialsException : RuntimeException()
