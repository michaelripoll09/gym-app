package com.gymapp.auth

fun passwordChangeError(current: String, next: String, confirmation: String): String? = when {
    current.isBlank() -> "Indica tu contrasena actual."
    next.length < 8 -> "La nueva contrasena debe tener al menos 8 caracteres."
    next != confirmation -> "Las nuevas contrasenas no coinciden."
    else -> null
}
