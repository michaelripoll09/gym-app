package com.gymapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordChangeStateTest {
    @Test fun `validates matching new passwords before sending`() {
        assertEquals("Las nuevas contrasenas no coinciden.", passwordChangeError("Passw0rd!", "NewPass1!", "OtherPass1!"))
        assertEquals("La nueva contrasena debe tener al menos 8 caracteres.", passwordChangeError("Passw0rd!", "short", "short"))
        assertNull(passwordChangeError("Passw0rd!", "NewPass1!", "NewPass1!"))
    }
}
