package com.gymapp.training

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrainingControllerIT(@Autowired private val json: ObjectMapper, @Autowired private val jdbc: JdbcTemplate, @LocalServerPort private val port: Int) {
    @BeforeEach
    fun seedExerciseFixtures() {
        seedExercise("00000000-0000-0000-0000-000000000001", "fixture-calisthenics", "Flexión de prueba", "CALISTHENICS")
        seedExercise("00000000-0000-0000-0000-000000000002", "fixture-bodybuilding", "Curl de prueba", "BODYBUILDING")
    }

    @BeforeEach
    fun seedProgressExerciseFixtures() {
        seedExercise("00000000-0000-0000-0000-000000000003", "fixture-progress-row", "Row test", "CALISTHENICS")
        seedExercise("00000000-0000-0000-0000-000000000004", "fixture-progress-squat", "Squat test", "CALISTHENICS")
    }

    @Test
    fun `lists a compatible curated plan and adopts an editable personal copy`() {
        val token = registerToken(); saveCalisthenicsProfile(token)

        val listed = request("GET", "/api/v1/curated-plans", token, null)

        assertEquals(HttpStatus.OK.value(), listed.statusCode())
        val plans = json.readValue(listed.body(), List::class.java) as List<Map<String, Any>>
        val plan = plans.single()
        assertEquals("Base de calistenia", plan.getValue("name"))
        assertEquals("Plan curado para principiante enfocado en ganar músculo.", plan.getValue("description"))
        assertEquals("CALISTHENICS", plan.getValue("primaryProfile"))
        assertEquals("BEGINNER", plan.getValue("experienceLevel"))
        assertEquals("MUSCLE_GAIN", plan.getValue("goal"))
        assertEquals(3, (plan.getValue("days") as List<*>).size)

        val adopted = request("POST", "/api/v1/curated-plans/${plan.getValue("id")}/adopt", token, emptyMap<String, Any>())
        val personal = request("GET", "/api/v1/workout-plans", token, null)

        assertEquals(HttpStatus.CREATED.value(), adopted.statusCode())
        assertEquals(1, (json.readValue(personal.body(), List::class.java) as List<*>).size)
    }

    @Test
    fun `rejects adopting a curated plan that does not match the profile`() {
        val token = registerToken(); saveCalisthenicsProfile(token)

        val response = request("POST", "/api/v1/curated-plans/starter-bodybuilding-beginner-muscle-gain/adopt", token, emptyMap<String, Any>())

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.statusCode())
    }

    @Test
    fun `returns an empty weekly summary when the user has no active training`() {
        val token = registerToken(); saveCalisthenicsProfile(token)

        val response = request("GET", "/api/v1/training-summary/weekly", token, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val summary = body(response)
        assertEquals(0, summary.getValue("completedSessions"))
        assertEquals(0, summary.getValue("scheduledSessions"))
        assertEquals(0, summary.getValue("adherencePercent"))
        assertEquals(0.0, summary.getValue("volumeKg"))
        assertEquals(null, summary["nextSession"])
    }

    @Test
    fun `summarizes current week adherence volume and next session`() {
        val token = registerToken(); saveCalisthenicsProfile(token)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Semana activa", "days" to listOf(
            mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 2, "minRepetitions" to 8, "maxRepetitions" to 8))),
            mapOf("name" to "MiÃ©rcoles", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 2, "minRepetitions" to 8, "maxRepetitions" to 8))),
        )))
        val session = request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", token, mapOf("sets" to listOf(
            mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 50.0),
            mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8),
        )))
        jdbc.update("update workout_sessions set started_at = ? where id = ?", OffsetDateTime.now(), UUID.fromString(body(session).getValue("id") as String))

        val response = request("GET", "/api/v1/training-summary/weekly", token, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val summary = body(response)
        assertEquals(1, summary.getValue("completedSessions"))
        assertEquals(2, summary.getValue("scheduledSessions"))
        assertEquals(50, summary.getValue("adherencePercent"))
        assertEquals(500.0, summary.getValue("volumeKg"))
        assertEquals("Semana activa", (summary.getValue("nextSession") as Map<*, *>)["planName"])
    }

    @Test
    fun `excludes volume from an archived plans session`() {
        val token = registerToken(); saveCalisthenicsProfile(token)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Archivada", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val planId = body(plan).getValue("id") as String
        request("POST", "/api/v1/workout-plans/$planId/sessions", token, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 30.0))))
        request("PUT", "/api/v1/workout-plans/$planId/archive", token, null)

        val summary = body(request("GET", "/api/v1/training-summary/weekly", token, null))

        assertEquals(0.0, summary.getValue("volumeKg"))
    }

    @Test
    fun `summarizes only the active routine when one is selected`() {
        val token = registerToken(); saveCalisthenicsProfile(token)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val active = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Fuerza activa", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val inactive = request("POST", "/api/v1/workout-plans", token, mapOf("name" to "Cardio alterno", "days" to listOf(
            mapOf("name" to "Martes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))),
            mapOf("name" to "Jueves", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))),
        )))
        val activeId = body(active).getValue("id") as String
        val inactiveId = body(inactive).getValue("id") as String
        request("PUT", "/api/v1/workout-plans/$activeId/activate", token, null)
        request("POST", "/api/v1/workout-plans/$activeId/sessions", token, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 20.0))))
        request("POST", "/api/v1/workout-plans/$inactiveId/sessions", token, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 90.0))))

        val summary = body(request("GET", "/api/v1/training-summary/weekly", token, null))

        assertEquals(1, summary.getValue("completedSessions"))
        assertEquals(1, summary.getValue("scheduledSessions"))
        assertEquals(200.0, summary.getValue("volumeKg"))
        assertEquals("Fuerza activa", (summary.getValue("nextSession") as Map<*, *>)["planName"])
    }

    @Test
    fun `returns increase maintain and reduce recommendations excluding archived routines`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exercises = listOf("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000004").map(UUID::fromString)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Progression", "days" to listOf(mapOf("name" to "Lunes", "exercises" to exercises.map { mapOf("exerciseId" to it.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12) }))))
        val planId = body(plan).getValue("id") as String
        val previous = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exercises[0].toString(), "repetitions" to 8, "loadKg" to 20.0), mapOf("exerciseId" to exercises[1].toString(), "repetitions" to 8, "loadKg" to 20.0), mapOf("exerciseId" to exercises[2].toString(), "repetitions" to 10, "loadKg" to 30.0))))
        val latest = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exercises[0].toString(), "repetitions" to 10, "loadKg" to 20.0), mapOf("exerciseId" to exercises[1].toString(), "repetitions" to 8, "loadKg" to 22.0), mapOf("exerciseId" to exercises[2].toString(), "repetitions" to 8, "loadKg" to 30.0))))
        jdbc.update("update workout_sessions set started_at = ? where id = ?", OffsetDateTime.parse("2026-07-28T10:00:00Z"), UUID.fromString(body(previous).getValue("id") as String))
        jdbc.update("update workout_sessions set started_at = ? where id = ?", OffsetDateTime.parse("2026-07-29T10:00:00Z"), UUID.fromString(body(latest).getValue("id") as String))
        val archived = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Archived", "days" to listOf(mapOf("name" to "Viernes", "exercises" to listOf(mapOf("exerciseId" to exercises[0].toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val archivedId = body(archived).getValue("id") as String
        request("POST", "/api/v1/workout-plans/$archivedId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exercises[0].toString(), "repetitions" to 20, "loadKg" to 40.0))))
        request("PUT", "/api/v1/workout-plans/$archivedId/archive", owner, null)

        val response = request("GET", "/api/v1/training-progress/recommendations", owner, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val recommendations = json.readValue(response.body(), List::class.java) as List<Map<String, Any>>
        assertEquals(setOf("INCREASE", "MAINTAIN", "REDUCE"), recommendations.map { it.getValue("action") }.toSet())
        assertEquals(false, recommendations.any { (it.getValue("latestLoadKg") as Number).toDouble() == 40.0 })
        assertEquals(false, recommendations.any { (it.getValue("explanation") as String).isBlank() })
    }

    @Test
    fun `returns an owners personal records with latest dates for tied marks`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Records propios", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        val planId = body(plan).getValue("id") as String
        val first = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 50.0))))
        val tiedLoad = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 12, "loadKg" to 50.0))))
        val repetitionRecord = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 20))))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-01T10:00:00Z"), UUID.fromString(body(first).getValue("id") as String))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-02T10:00:00Z"), UUID.fromString(body(tiedLoad).getValue("id") as String))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-03T10:00:00Z"), UUID.fromString(body(repetitionRecord).getValue("id") as String))
        val other = registerToken(); saveCalisthenicsProfile(other)
        val otherPlan = request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Record ajeno", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        request("POST", "/api/v1/workout-plans/${body(otherPlan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 99, "loadKg" to 200.0))))

        val response = request("GET", "/api/v1/training-progress/personal-records", owner, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        val record = (json.readValue(response.body(), List::class.java) as List<Map<String, Any>>).single()
        assertEquals("Flexión de prueba", record.getValue("exerciseName"))
        assertEquals(50.0, record.getValue("maximumLoadKg"))
        assertEquals("2026-08-02T10:00Z", record.getValue("maximumLoadAt"))
        assertEquals(20, record.getValue("maximumRepetitions"))
        assertEquals("2026-08-03T10:00Z", record.getValue("maximumRepetitionsAt"))
    }

    @Test
    fun `recalculates personal records after correcting or deleting a session`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Records corregibles", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        val planId = body(plan).getValue("id") as String
        request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 40.0))))
        val highest = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 12, "loadKg" to 50.0))))
        val highestId = body(highest).getValue("id") as String

        request("PUT", "/api/v1/workout-sessions/$highestId", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8, "loadKg" to 45.0))))
        val corrected = (json.readValue(request("GET", "/api/v1/training-progress/personal-records", owner, null).body(), List::class.java) as List<Map<String, Any>>).single()
        request("DELETE", "/api/v1/workout-sessions/$highestId", owner, null)
        val deleted = (json.readValue(request("GET", "/api/v1/training-progress/personal-records", owner, null).body(), List::class.java) as List<Map<String, Any>>).single()

        assertEquals(45.0, corrected.getValue("maximumLoadKg"))
        assertEquals(10, corrected.getValue("maximumRepetitions"))
        assertEquals(40.0, deleted.getValue("maximumLoadKg"))
        assertEquals(10, deleted.getValue("maximumRepetitions"))
    }

    @Test
    fun `session creation reports only strict personal record milestones for its owner`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Hitos", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        val planId = body(plan).getValue("id") as String

        val first = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 40.0))))
        val tied = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 40.0))))
        val improved = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 12, "loadKg" to 45.0))))
        val other = registerToken(); saveCalisthenicsProfile(other)
        val otherPlan = request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Ajeno", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        request("POST", "/api/v1/workout-plans/${body(otherPlan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 99, "loadKg" to 200.0))))

        val firstMilestones = body(first).getValue("milestones") as List<Map<String, Any>>
        val tiedMilestones = body(tied).getValue("milestones") as List<Map<String, Any>>
        val improvedMilestones = body(improved).getValue("milestones") as List<Map<String, Any>>
        assertEquals(setOf("LOAD", "REPETITIONS"), firstMilestones.map { it.getValue("type") }.toSet())
        assertEquals(emptyList<Any>(), tiedMilestones)
        assertEquals(setOf("LOAD", "REPETITIONS"), improvedMilestones.map { it.getValue("type") }.toSet())
        assertEquals(setOf(45.0, 12.0), improvedMilestones.map { (it.getValue("value") as Number).toDouble() }.toSet())
        assertTrue(improvedMilestones.all { it.getValue("exerciseName").toString().isNotBlank() && it.getValue("achievedAt").toString().isNotBlank() })
    }

    @Test
    fun `private progress analysis combines recent training measurements goals and records`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Analisis", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        val planId = body(plan).getValue("id") as String
        request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 12, "loadKg" to 45.0))))
        request("POST", "/api/v1/body-measurements", owner, mapOf("recordedOn" to "2026-08-01", "weightKg" to 80.0))
        request("POST", "/api/v1/body-measurements", owner, mapOf("recordedOn" to "2026-08-10", "weightKg" to 79.0))
        request("POST", "/api/v1/progress-goals", owner, mapOf("type" to "EXERCISE_LOAD", "targetValue" to 60.0, "exerciseName" to "Flexión de prueba"))

        val response = request("GET", "/api/v1/training-progress/analysis", owner, null)
        val analysis = body(response)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals(1, analysis.getValue("completedSessions"))
        assertEquals(-1.0, analysis.getValue("weightChangeKg"))
        assertEquals(1, analysis.getValue("activeGoals"))
        assertEquals(1, analysis.getValue("recentPersonalRecords"))
        assertEquals(true, analysis.getValue("sufficientData"))
        assertTrue((analysis.getValue("sources") as List<*>).any { it.toString().contains("sesiones") })
    }

    @Test
    fun `private progress analysis reports insufficient data without exposing another account`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val other = registerToken(); saveCalisthenicsProfile(other)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Ajeno", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 99, "loadKg" to 200.0))))

        val response = request("GET", "/api/v1/training-progress/analysis", owner, null)
        val analysis = body(response)

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals(0, analysis.getValue("completedSessions"))
        assertEquals(0, analysis.getValue("recentPersonalRecords"))
        assertEquals(false, analysis.getValue("sufficientData"))
    }

    @Test
    fun `routine review suggests an active owners compatible exercise from its progress`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Rutina activa", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 12))))))
        val planId = body(plan).getValue("id") as String
        request("PUT", "/api/v1/workout-plans/$planId/activate", owner, null)
        val first = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8, "loadKg" to 20.0))))
        val latest = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 20.0))))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-01T10:00:00Z"), UUID.fromString(body(first).getValue("id") as String))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-02T10:00:00Z"), UUID.fromString(body(latest).getValue("id") as String))

        val response = request("GET", "/api/v1/training-progress/routine-review", owner, null)
        val review = body(response)
        val suggestion = (review.getValue("suggestions") as List<Map<String, Any>>).single()

        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals("READY", review.getValue("state"))
        assertEquals("Flexión de prueba", suggestion.getValue("exerciseName"))
        assertEquals("CONSIDER_PROGRESSING", suggestion.getValue("action"), suggestion.toString())
        assertTrue((suggestion.getValue("sources") as List<*>).isNotEmpty())
    }

    @Test
    fun `routine review reports no active plan without exposing another account`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val other = registerToken(); saveCalisthenicsProfile(other)
        val response = request("GET", "/api/v1/training-progress/routine-review", owner, null)
        val review = body(response)
        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals("NO_ACTIVE_PLAN", review.getValue("state"))
        assertEquals(emptyList<Any>(), review.getValue("suggestions"))
    }

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
    fun `selects one active plan and rejects archived or another users plans`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val first = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Fuerza", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val second = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Cardio", "days" to listOf(mapOf("name" to "Martes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val firstId = body(first).getValue("id") as String
        val secondId = body(second).getValue("id") as String

        val activateFirst = request("PUT", "/api/v1/workout-plans/$firstId/activate", owner, null)
        val activateSecond = request("PUT", "/api/v1/workout-plans/$secondId/activate", owner, null)
        val listed = json.readValue(request("GET", "/api/v1/workout-plans", owner, null).body(), List::class.java) as List<Map<String, Any>>
        request("PUT", "/api/v1/workout-plans/$firstId/archive", owner, null)
        val archived = request("PUT", "/api/v1/workout-plans/$firstId/activate", owner, null)
        val other = registerToken(); saveCalisthenicsProfile(other)
        val foreign = request("PUT", "/api/v1/workout-plans/$secondId/activate", other, null)

        assertEquals(HttpStatus.NO_CONTENT.value(), activateFirst.statusCode())
        assertEquals(HttpStatus.NO_CONTENT.value(), activateSecond.statusCode())
        assertEquals(listOf("Cardio"), listed.filter { it["active"] == true }.map { it.getValue("name") })
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), archived.statusCode())
        assertEquals(HttpStatus.FORBIDDEN.value(), foreign.statusCode())
    }

    @Test
    fun `returns an owners monthly completed history and active schedule only`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Calendario propio", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val planId = body(plan).getValue("id") as String
        request("PUT", "/api/v1/workout-plans/$planId/activate", owner, null)
        val session = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10))))
        jdbc.update("update workout_sessions set started_at=? where id=?", OffsetDateTime.parse("2026-08-03T10:00:00Z"), UUID.fromString(body(session).getValue("id") as String))
        val other = registerToken(); saveCalisthenicsProfile(other)
        val response = request("GET", "/api/v1/training-calendar?month=2026-08&zone=UTC", owner, null)

        assertEquals(HttpStatus.OK.value(), response.statusCode(), response.body())
        val days = json.readValue(response.body(), List::class.java) as List<Map<String, Any>>
        assertEquals(31, days.size)
        assertEquals(true, days.single { it["date"] == "2026-08-03" }["completed"])
        assertEquals(true, days.single { it["date"] == "2026-08-10" }["scheduled"])
        assertEquals(false, days.any { it["planName"] == "Sesion ajena" })
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

    @Test
    fun `persists an owners optional perceived effort and note and rejects an invalid effort`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion con esfuerzo", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val planId = body(plan).getValue("id")

        val created = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10)), "perceivedExertion" to 8, "note" to "Buena tecnica"))
        val other = registerToken(); saveCalisthenicsProfile(other)
        val otherPlan = request("POST", "/api/v1/workout-plans", other, mapOf("name" to "Sesion privada", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        request("POST", "/api/v1/workout-plans/${body(otherPlan).getValue("id")}/sessions", other, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10)), "perceivedExertion" to 4, "note" to "Nota privada"))
        val listed = request("GET", "/api/v1/workout-sessions", owner, null)
        val invalid = request("POST", "/api/v1/workout-plans/$planId/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10)), "perceivedExertion" to 11))

        assertEquals(HttpStatus.CREATED.value(), created.statusCode())
        assertEquals(HttpStatus.OK.value(), listed.statusCode())
        val session = (json.readValue(listed.body(), List::class.java) as List<Map<String, Any>>).single()
        assertEquals(8, session.getValue("perceivedExertion"))
        assertEquals("Buena tecnica", session.getValue("note"))
        assertEquals("Sesion con esfuerzo", session.getValue("planName"))
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), invalid.statusCode())
    }

    @Test
    fun `updates an owners completed session with corrected sets and feedback`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion corregible", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val created = request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8, "loadKg" to 20.0)), "perceivedExertion" to 5, "note" to "Inicial"))
        val sessionId = body(created).getValue("id") as String

        val updated = request("PUT", "/api/v1/workout-sessions/$sessionId", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 10, "loadKg" to 30.0)), "perceivedExertion" to 8, "note" to "Tecnica corregida"))
        val listed = request("GET", "/api/v1/workout-sessions", owner, null)

        assertEquals(HttpStatus.NO_CONTENT.value(), updated.statusCode())
        val session = (json.readValue(listed.body(), List::class.java) as List<Map<String, Any>>).single()
        val set = (session.getValue("sets") as List<Map<String, Any>>).single()
        assertEquals(10, set.getValue("repetitions"))
        assertEquals(30.0, set.getValue("loadKg"))
        assertEquals(8, session.getValue("perceivedExertion"))
        assertEquals("Tecnica corregida", session.getValue("note"))
    }

    @Test
    fun `rejects invalid corrections and hides another owners session`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion privada", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val sessionId = body(request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8))))).getValue("id") as String
        val other = registerToken(); saveCalisthenicsProfile(other)

        val invalid = request("PUT", "/api/v1/workout-sessions/$sessionId", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 0)), "perceivedExertion" to 11))
        val foreign = request("DELETE", "/api/v1/workout-sessions/$sessionId", other, null)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), invalid.statusCode())
        assertEquals(HttpStatus.NOT_FOUND.value(), foreign.statusCode())
        assertEquals(1, (json.readValue(request("GET", "/api/v1/workout-sessions", owner, null).body(), List::class.java) as List<*>).size)
    }

    @Test
    fun `deletes an owners completed session`() {
        val owner = registerToken(); saveCalisthenicsProfile(owner)
        val exerciseId = jdbc.queryForObject("select e.id from exercises e join exercise_training_profiles p on p.exercise_id=e.id where p.profile_code='CALISTHENICS' limit 1", UUID::class.java)
        val plan = request("POST", "/api/v1/workout-plans", owner, mapOf("name" to "Sesion eliminable", "days" to listOf(mapOf("name" to "Lunes", "exercises" to listOf(mapOf("exerciseId" to exerciseId.toString(), "sets" to 1, "minRepetitions" to 8, "maxRepetitions" to 8))))))
        val sessionId = body(request("POST", "/api/v1/workout-plans/${body(plan).getValue("id")}/sessions", owner, mapOf("sets" to listOf(mapOf("exerciseId" to exerciseId.toString(), "repetitions" to 8))))).getValue("id") as String

        val deleted = request("DELETE", "/api/v1/workout-sessions/$sessionId", owner, null)

        assertEquals(HttpStatus.NO_CONTENT.value(), deleted.statusCode())
        assertEquals(emptyList<Any>(), json.readValue(request("GET", "/api/v1/workout-sessions", owner, null).body(), List::class.java))
    }

    private fun registerToken(): String = (body(request("POST", "/api/v1/auth/register", null, mapOf("email" to "training-${UUID.randomUUID()}@example.com", "password" to "Passw0rd!", "acceptedTermsAt" to "2026-07-27T00:00:00Z"))).getValue("accessToken") as String)
    private fun seedExercise(id: String, sourceId: String, name: String, profile: String) {
        jdbc.update("insert into exercises (id, source_name, source_external_id, source_commit, name, spanish_instructions, published, source_file_sha256, attribution, review_status) values (?, 'test-fixture', ?, 'test', ?, 'Instrucción de prueba', true, '', null, 'APPROVED') on conflict (source_name, source_external_id) do nothing", UUID.fromString(id), sourceId, name)
        jdbc.update("insert into exercise_training_profiles (exercise_id, profile_code) values (?, ?) on conflict do nothing", UUID.fromString(id), profile)
    }
    private fun saveCalisthenicsProfile(token: String) { request("PUT", "/api/v1/me/training-profile", token, mapOf("experienceLevel" to "BEGINNER", "primaryProfile" to "CALISTHENICS", "secondaryProfiles" to emptyList<String>(), "goal" to "MUSCLE_GAIN", "availabilityBand" to "MEDIUM", "availableDaysPerWeek" to 3, "sessionDurationMinutes" to 60)) }
    private fun request(method: String, path: String, token: String?, payload: Any?): HttpResponse<String> { val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).header("Content-Type", "application/json"); if (token != null) builder.header("Authorization", "Bearer $token"); val request = when (method) { "GET" -> builder.GET().build(); "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build(); "DELETE" -> builder.DELETE().build(); else -> builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build() }; return client.send(request, HttpResponse.BodyHandlers.ofString()) }
    @Suppress("UNCHECKED_CAST") private fun body(response: HttpResponse<String>) = json.readValue(response.body(), Map::class.java) as Map<String, Any>
    private companion object { val client: HttpClient = HttpClient.newHttpClient() }
}
