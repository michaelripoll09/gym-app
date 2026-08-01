package com.gymapp.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDeletionStateTest {
    @Test fun `requires explicit confirmation before submitting account deletion`() {
        val requested = requestAccountDeletion(AccountDeletionState())

        assertTrue(requested.confirming)
        assertFalse(requested.deleting)
        assertEquals(AccountDeletionState(), cancelAccountDeletion(requested))
    }

    @Test fun `keeps an account deletion failure recoverable`() {
        val failed = accountDeletionResult(AccountDeletionState(confirming = true, deleting = true), success = false)

        assertEquals("No pudimos eliminar tu cuenta. Reintenta.", failed.error)
        assertTrue(failed.confirming)
        assertFalse(failed.deleting)
    }

    @Test fun `starts deletion only after confirming`() {
        assertTrue(confirmAccountDeletion(AccountDeletionState(confirming = true)).deleting)
    }

    @Test fun `clears every local account store after successful deletion`() {
        var sessionCleared = false
        var offlineCleared = false
        var remindersCleared = false

        clearLocalAccountData({ sessionCleared = true }, { offlineCleared = true }, { remindersCleared = true })

        assertTrue(sessionCleared)
        assertTrue(offlineCleared)
        assertTrue(remindersCleared)
    }
}
