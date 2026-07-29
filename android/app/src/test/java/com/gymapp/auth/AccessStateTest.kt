package com.gymapp.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AccessStateTest {
    @Test
    fun `switches from registration to login without retaining an error`() {
        val login = AccessState(mode = AccessMode.REGISTER, error = "Correo ocupado").toggleMode()

        assertEquals(AccessMode.LOGIN, login.mode)
        assertEquals(null, login.error)
    }

    @Test
    fun `shows a retryable message for invalid login credentials`() {
        assertEquals("Correo o contraseña incorrectos", accessErrorMessage(AccessMode.LOGIN))
    }

    @Test
    fun `requires clearing the local session only for unauthorized responses`() {
        assertEquals(true, requiresSessionReset(401))
        assertEquals(false, requiresSessionReset(500))
        assertEquals(false, requiresSessionReset(null))
    }
}
