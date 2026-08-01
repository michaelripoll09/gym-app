package com.gymapp.auth

private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun passwordResetRequestError(email: String): String? = if (emailPattern.matches(email.trim())) null else "Ingresa un correo válido."

fun passwordResetConfirmationError(password: String, confirmation: String): String? = when {
    password.length < 8 -> "La nueva contraseña debe tener al menos 8 caracteres."
    password != confirmation -> "Las contraseñas no coinciden."
    else -> null
}
