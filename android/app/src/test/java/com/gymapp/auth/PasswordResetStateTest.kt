package com.gymapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordResetStateTest {
    @Test fun `requires a valid email and matching strong replacement password`() {
        assertEquals("Ingresa un correo válido.", passwordResetRequestError("correo"))
        assertEquals("La nueva contraseña debe tener al menos 8 caracteres.", passwordResetConfirmationError("short", "short"))
        assertEquals("Las contraseñas no coinciden.", passwordResetConfirmationError("NewPass1!", "OtherPass1!"))
        assertNull(passwordResetConfirmationError("NewPass1!", "NewPass1!"))
    }
}
