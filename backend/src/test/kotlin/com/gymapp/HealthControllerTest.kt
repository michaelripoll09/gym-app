package com.gymapp

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class HealthControllerTest(@Autowired context: WebApplicationContext) {
    private val mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(context).build()

    @Test
    fun `returns service health`() {
        mockMvc.get("/api/v1/health") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("ok") }
        }
    }
}
