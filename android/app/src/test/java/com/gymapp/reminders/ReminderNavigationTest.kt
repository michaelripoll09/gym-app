package com.gymapp.reminders

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderNavigationTest {
    @Test fun `notification launch requests today training`() {
        assertEquals(ReminderDestination.TODAY, reminderDestination(true))
    }

    @Test fun `regular app launch keeps the catalog destination`() {
        assertEquals(ReminderDestination.CATALOG, reminderDestination(false))
    }
}
