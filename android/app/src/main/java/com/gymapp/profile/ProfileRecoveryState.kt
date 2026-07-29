package com.gymapp.profile

import com.gymapp.network.TrainingProfileResponse

sealed interface ProfileRecoveryState {
    data object Loading : ProfileRecoveryState
    data class Existing(val primaryProfile: String) : ProfileRecoveryState
    data object NeedsOnboarding : ProfileRecoveryState
    data object Unauthorized : ProfileRecoveryState
    data object RetryableFailure : ProfileRecoveryState
}

fun resolveProfileRecovery(profile: TrainingProfileResponse?, statusCode: Int?): ProfileRecoveryState = when {
    profile != null -> ProfileRecoveryState.Existing(profile.primaryProfile)
    statusCode == 404 -> ProfileRecoveryState.NeedsOnboarding
    statusCode == 401 -> ProfileRecoveryState.Unauthorized
    else -> ProfileRecoveryState.RetryableFailure
}
