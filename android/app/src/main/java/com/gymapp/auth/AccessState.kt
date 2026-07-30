package com.gymapp.auth

enum class AccessMode { REGISTER, LOGIN }

data class AccessState(val mode: AccessMode = AccessMode.REGISTER, val error: String? = null) {
    fun toggleMode() = copy(mode = if (mode == AccessMode.REGISTER) AccessMode.LOGIN else AccessMode.REGISTER, error = null)
}

fun accessErrorMessage(mode: AccessMode) = if (mode == AccessMode.LOGIN) "Correo o contraseña incorrectos" else "No fue posible crear la cuenta"

fun requiresSessionReset(statusCode: Int?) = statusCode == 401
