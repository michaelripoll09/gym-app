package com.gymapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutStateTest {
    @Test fun `requires confirmation before closing a session`() {
        val requested = requestLogout(LogoutState())

        assertTrue(requested.confirming)
        assertFalse(requested.loggingOut)
        assertEquals(LogoutState(), cancelLogout(requested))
    }

    @Test fun `keeps logout failure recoverable`() {
        val failed = logoutResult(LogoutState(confirming = true, loggingOut = true), success = false)

        assertEquals("No pudimos cerrar tu sesion. Reintenta.", failed.error)
        assertTrue(failed.confirming)
        assertFalse(failed.loggingOut)
    }
}
