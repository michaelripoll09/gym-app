package com.gymapp.curated

import com.gymapp.network.CuratedPlanResponse

enum class CuratedPlansContent { LOADING, ERROR, EMPTY, READY }

data class CuratedPlansState(
    val plans: List<CuratedPlanResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content(): CuratedPlansContent = when {
        loading -> CuratedPlansContent.LOADING
        error != null -> CuratedPlansContent.ERROR
        plans.isEmpty() -> CuratedPlansContent.EMPTY
        else -> CuratedPlansContent.READY
    }
}
