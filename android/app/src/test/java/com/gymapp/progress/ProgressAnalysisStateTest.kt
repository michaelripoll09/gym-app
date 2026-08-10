package com.gymapp.progress

import com.gymapp.network.ProgressAnalysisResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressAnalysisStateTest {
    @Test
    fun `analysis state explains when the account needs more data`() {
        val state = ProgressAnalysisState(analysis = ProgressAnalysisResponse(28, 0, 0, null, null, 0, 0, 0, false, listOf("0 sesiones sincronizadas")))

        assertEquals(ProgressAnalysisContent.EMPTY, state.content())
        assertEquals("Registra sesiones, medidas u objetivos para recibir un análisis de progreso.", state.emptyMessage())
    }

    @Test
    fun `analysis state is ready when backend provides enough private data`() {
        val state = ProgressAnalysisState(analysis = ProgressAnalysisResponse(28, 3, 4, 75, -0.5, 1, 1, 2, true, listOf("3 sesiones sincronizadas")))

        assertEquals(ProgressAnalysisContent.READY, state.content())
        assertEquals("-0.5 kg", formatWeightChange(state.analysis!!.weightChangeKg!!))
    }
}
