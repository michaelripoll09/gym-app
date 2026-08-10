package com.gymapp.progress

import com.gymapp.network.PersonalRecordResponse

enum class PersonalRecordsContent { LOADING, ERROR, EMPTY, READY }

data class PersonalRecordsState(
    val records: List<PersonalRecordResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
) {
    fun content() = when {
        loading -> PersonalRecordsContent.LOADING
        error != null -> PersonalRecordsContent.ERROR
        records.isEmpty() -> PersonalRecordsContent.EMPTY
        else -> PersonalRecordsContent.READY
    }
}
