package com.gymapp.training

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrainingControllerIT(@Autowired private val json: ObjectMapper, @Autowired private val jdbc: JdbcTemplate, @LocalServerPort private val port: Int) {
    @Test
    fun `archives and restores an owners plan without deleting its sessions`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val created = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Archivable", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val planId = body(created).getValue("id") as String
        request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8))))

        val archived = request("PUT", "/api/v1/workout-plans/$planId/archive", owner, null)
        val active = request("GET", "/api/v1/workout-plans", owner, null)
        val sessions = request("GET", "/api/v1/workout-sessions", owner, null)
        val restored = request("PUT", "/api/v1/workout-plans/$planId/restore", owner, null)

        assertEquals(HttpStatus.NO_CONTENT.value(), archived.statusCode())
        assertEquals(emptyList<Any>(), json.readValue(active.body(), List::class.java))
        assertEquals(1, (json.readValue(sessions.body(), List::class.java) as List<*>).size)
        assertEquals(HttpStatus.NO_CONTENT.value(), restored.statusCode())
    }
    @Test
    fun `updates an owners workout plan without creating another`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val created = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Original", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val planId = body(created).getValue("id") as String

        val updated = request("PUT", "/api/v1/workout-plans/$planId", owner, mapOf("name" to "Actualizada", "days" to listOf(mapOf("name" to "Viernes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 4, "minRepetitions" to 12, "maxRepetitions" to 12, "restSeconds" to 90))))))
        val listed = request("GET", "/api/v1/workout-plans", owner, null)

        assertEquals(HttpStatus.NO_CONTENT.value(), updated.statusCode())
        val plans = json.readValue(listed.body(), List::class.java) as List<Map<String, Any>>
        assertEquals(listOf("Actualizada"), plans.map { it.getValue("name") })
    }

    @Test
    fun `rejects updating another users workout plan`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Privada", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val other = registerToken(); saveCalisthenicsProfile(other)

        val response = request("PUT", "/api/v1/workout-plans/${body(plan).getValue("id")}", other, mapOf("name" to "Intrusa", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))

        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode())
    }
    @Test
    fun `creates a calisthenics plan and records two sets`() {
        val token = registerToken()
        saveCalisthenicsProfile(token)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id = e.id where p.profile_code = 'CALISTHENICS' limit 1", UUID::class.java)
        val created = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Base calistenia", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12))))))

        assertEquals(HttpStatus.CREATED.value(), created.statusCode())
        val planId = body(created).getValue("id") as String
        val session = request("POST", "/api/v1/workout-plans/$planId/sessions", token, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10), mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 9))))
        assertEquals(HttpStatus.CREATED.value(), session.statusCode())
    }

    @Test
    fun `rejects an exercise not tagged for the primary profile`() {
        val token = registerToken(); saveCalisthenicsProfile(token)
        val incompatible = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='BODYBUILDING' and not exists (select 1 from exercise_training_profiles cp where cp.exercise_id=e.id and cp.profile_code='CALISTHENICS') limit 1", UUID::class.java)
        val response = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Incompatible", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to incompatible.toString(), "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode())
    }

    @Test
    fun `rejects a session on another users plan`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Owner plan", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 1, "maxRepetitions" to 1))))))
        val other = registerToken()
        val response = request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 1))))
        assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode())
    }

    @Test
    fun `lists only the authenticated users persisted workout plans`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Plan persistido", "days" to listOf(mapOf("name" to "Día A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 3, "minRepetitions" to 8, "maxRepetitions" to 12, "restSeconds" to 90))))))
        val other = registerToken(); saveCalisthenicsProfile(other)
        request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Plan ajeno", "days" to listOf(mapOf("name" to "Día B", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 2, "minRepetitions" to 10, "maxRepetitions" to 10))))))

        val response = request("GET", "/api/v1/workout-plans", owner, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val plans = json.readValue(response.body(), List::class.java) as List<Map<String, Any>>
        assertEquals(listOf("Plan persistido"), plans.map { it.getValue("name") })
        val days = plans.first().getValue("days") as List<Map<String, Any>>
        val exercises = days.first().getValue("exercises") as List<Map<String, Any>>
        assertEquals(90, exercises.first().getValue("restSeconds"))
    }

    @Test
    fun `lists owner sessions newest first with logged repetitions and excludes another owner`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val olderPlan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion antigua", "days" to listOf(mapOf("name" to "A", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val newerPlan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion reciente", "days" to listOf(mapOf("name" to "B", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val older = request("POST", "/api/v1/workout-plans/${body(olderPlan).getValue("id")}/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8))))
        val newer = request("POST", "/api/v1/workout-plans/${body(newerPlan).getValue("id")}/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 42.5))))
        jdbc.update("update workout_sessions set started_at = ? where id = ?", OffsetDateTime.parse("2026-07-27T10:00:00Z"), UUID.fromString(body(older).getValue("id") as String))
        jdbc.update("update workout_sessions set started_at = ? where id = ?", OffsetDateTime.parse("2026-07-28T10:00:00Z"), UUID.fromString(body(newer).getValue("id") as String))
        val other = registerToken(); saveCalisthenicsProfile(other)
        val otherPlan = request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Sesion ajena", "days" to listOf(mapOf("name" to "C", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        request("POST", "/api/v1/workout-plans/${body(otherPlan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 12))))
        val response = request("GET", "/api/v1/workout-sessions", owner, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val sessions = json.readValue(response.body(), List::class.java) as List<Map<String, Any>>
        assertEquals(listOf("Sesion reciente", "Sesion antigua"), sessions.map { it.getValue("planName") })
        val newestSets = sessions.first().getValue("sets") as List<Map<String, Any>>
        assertEquals(10, newestSets.single().getValue("repetitions"))
        assertEquals(42.5, newestSets.single().getValue("loadKg"))
        assertEquals(false, sessions.any { it.getValue("planName") == "Sesion ajena" })
    }

    private fun registerToken(): String = (body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "training-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-07-27T00:00:00Z"))).getValue("accessToken") as String)
    private fun saveCalisthenicsProfile(token: String) { request("PUT", "/api/v1/me/training-profile", token, mapOf("experienceLevel" to "BEGINNER", "primaryProfile" to "CALISTHENICS", "secondaryProfiles" to emptyList<String>(), "goal" to "MUSCLE_GAIN", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 3, "sessionDurationMinutes" to 60)) }
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> { val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json"); if (token != null) builder.header("Authorization", "Bearer $token"); val request = when (method) { "GET" -> builder.GET().build(); "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build(); else -> builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build() }; return client.send(request, HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
