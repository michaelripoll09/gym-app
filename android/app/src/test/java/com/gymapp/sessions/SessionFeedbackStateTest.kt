package com.gymapp.sessions

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionFeedbackStateTest {
    @Test
    fun `accepts an optional effort from one through ten`() {
        assertEquals(null, sessionFeedbackError(""))
        assertEquals(null, sessionFeedbackError("1"))
        assertEquals(null, sessionFeedbackError("10"))
    }

    @Test
    fun `rejects an effort outside the supported range`() {
        assertEquals("Selecciona un esfuerzo entre 1 y 10", sessionFeedbackError("11"))
    }
}
