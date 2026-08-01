package com.gymapp.auth

data class LogoutState(
    val confirming: Boolean = false,
    val loggingOut: Boolean = false,
    val error: String? = null,
)

fun requestLogout(state: LogoutState) = state.copy(confirming = true, error = null)
fun cancelLogout(state: LogoutState) = state.copy(confirming = false, loggingOut = false, error = null)
fun logoutResult(state: LogoutState, success: Boolean) = if (success) LogoutState() else state.copy(loggingOut = false, error = "No pudimos cerrar tu sesion. Reintenta.")
