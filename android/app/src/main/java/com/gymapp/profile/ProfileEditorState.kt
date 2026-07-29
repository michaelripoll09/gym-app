package com.gymapp.profile

import com.gymapp.network.TrainingProfileResponse
import com.gymapp.onboarding.ProfileSelectionState
import com.gymapp.onboarding.TrainingProfile

sealed interface ProfileEditorState {
    data object Loading : ProfileEditorState
    data class Editing(val selection: ProfileSelectionState) : ProfileEditorState
    data object Unauthorized : ProfileEditorState
    data object RetryableFailure : ProfileEditorState
}

fun resolveProfileEditor(profile: TrainingProfileResponse?, statusCode: Int?): ProfileEditorState = when {
    profile != null -> ProfileEditorState.Editing(
        ProfileSelectionState(
            experienceLevel = profile.experienceLevel,
            primary = TrainingProfile.entries.firstOrNull { it.name == profile.primaryProfile },
            secondaryProfiles = profile.secondaryProfiles.mapNotNull { stored ->
                TrainingProfile.entries.firstOrNull { it.name == stored }
            }.toSet(),
            goal = profile.goal,
            availabilityBand = profile.availabilityBand,
            days = profile.availableDaysPerWeek,
            minutes = profile.sessionDurationMinutes,
        ),
    )
    statusCode == 401 -> ProfileEditorState.Unauthorized
    else -> ProfileEditorState.RetryableFailure
}

fun profileForUpdatedCatalog(primaryProfile: String): String = primaryProfile
