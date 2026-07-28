package com.gymapp.auth

import android.content.Context

class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("gym_app_session", Context.MODE_PRIVATE)
    fun save(token: String) { preferences.edit().putString("access_token", token).apply() }
    fun read(): String? = preferences.getString("access_token", null)
    fun clear() { preferences.edit().remove("access_token").apply() }
}
