package com.gymapp.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class TokenStoreSecurityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun removesLegacyPlaintextTokenAndRequiresReauthentication() {
        val legacy = context.getSharedPreferences("gym_app_session", Context.MODE_PRIVATE)
        legacy.edit().putString("access_token", UUID.randomUUID().toString()).apply()

        val store = TokenStore(context)

        assertNull(store.read())
        assertFalse(legacy.contains("access_token"))
    }

    @Test
    fun storesSessionTokenOutsideLegacyPlaintextPreferences() {
        val legacy = context.getSharedPreferences("gym_app_session", Context.MODE_PRIVATE)
        legacy.edit().clear().apply()
        val store = TokenStore(context)
        val sessionToken = UUID.randomUUID().toString()

        store.save(sessionToken)

        assertEquals(sessionToken, store.read())
        assertFalse(legacy.contains("access_token"))
    }
}