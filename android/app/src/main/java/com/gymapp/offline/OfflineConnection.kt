package com.gymapp.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun isOffline(context: Context): Boolean {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    val network = manager.activeNetwork ?: return true
    return manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true
}
