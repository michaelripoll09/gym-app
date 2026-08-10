package com.gymapp.progress

import com.gymapp.network.PersonalRecordResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalRecordsStateTest {
    @Test
    fun `uses loading empty error and ready states for personal records`() {
        val record = PersonalRecordResponse("Sentadilla", 100.0, "2026-08-02T10:00:00Z", 12, "2026-08-03T10:00:00Z")

        assertEquals(PersonalRecordsContent.LOADING, PersonalRecordsState(loading = true).content())
        assertEquals(PersonalRecordsContent.EMPTY, PersonalRecordsState().content())
        assertEquals(PersonalRecordsContent.ERROR, PersonalRecordsState(error = "Sin conexion").content())
        assertEquals(PersonalRecordsContent.READY, PersonalRecordsState(records = listOf(record)).content())
    }
}
