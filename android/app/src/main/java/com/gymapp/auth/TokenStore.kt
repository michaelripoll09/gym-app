package com.gymapp.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStore(context: Context) {
    private val legacyPreferences = context.getSharedPreferences(LEGACY_PREFERENCES, Context.MODE_PRIVATE)
    private val securePreferences = legacyPreferences.run {
        edit().remove(ACCESS_TOKEN).apply()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFERENCES,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun save(token: String) { securePreferences.edit().putString(ACCESS_TOKEN, token).apply() }
    fun read(): String? = securePreferences.getString(ACCESS_TOKEN, null)
    fun saveProfile(profile: String) { legacyPreferences.edit().putString(PRIMARY_PROFILE, profile).apply() }
    fun readProfile(): String? = legacyPreferences.getString(PRIMARY_PROFILE, null)
    fun clear() {
        securePreferences.edit().remove(ACCESS_TOKEN).apply()
        legacyPreferences.edit().remove(PRIMARY_PROFILE).apply()
    }

    private companion object {
        const val LEGACY_PREFERENCES = "gym_app_session"
        const val SECURE_PREFERENCES = "gym_app_secure_session"
        const val ACCESS_TOKEN = "access_token"
        const val PRIMARY_PROFILE = "primary_profile"
    }
}