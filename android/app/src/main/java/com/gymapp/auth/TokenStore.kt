package com.gymapp.auth

import android.content.Context

class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("gym_app_session", Context.MODE_PRIVATE)
    fun save(token: String) { preferences.edit().putString("access_token", token).apply() }
    fun read(): String? = preferences.getString("access_token", null)
    fun saveProfile(profile: String) { preferences.edit().putString("primary_profile", profile).apply() }
    fun readProfile(): String? = preferences.getString("primary_profile", null)
    fun clear() { preferences.edit().remove("access_token").remove("primary_profile").apply() }
}
