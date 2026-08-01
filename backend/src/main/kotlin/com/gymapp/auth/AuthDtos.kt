package com.gymapp.auth

import java.time.Instant
import java.util.UUID

data class RegisterRequest(val email: String, val password: String, val acceptedTermsAt: Instant)
data class LoginRequest(val email: String, val password: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class AuthResponse(val userId: UUID, val accessToken: String)
data class MeResponse(val userId: UUID, val email: String)
