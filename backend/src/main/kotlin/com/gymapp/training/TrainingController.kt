package com.gymapp.training

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

data class ExercisePlanRequest(val exerciseId: UUID, val sets: Int, val minRepetitions: Int, val maxRepetitions: Int, val restSeconds: Int? = null)
data class WorkoutDayRequest(val name: String, val exercises: List<ExercisePlanRequest>)
data class CreateWorkoutPlanRequest(val name: String, val days: List<WorkoutDayRequest>)
data class SetLogRequest(val exerciseId: UUID, val repetitions: Int, val loadKg: Double? = null)
data class CreateWorkoutSessionRequest(val sets: List<SetLogRequest>, val perceivedExertion: Int? = null, val note: String? = null)
data class UpdateWorkoutSessionRequest(val sets: List<SetLogRequest>, val perceivedExertion: Int? = null, val note: String? = null)
data class IdResponse(val id: UUID)
data class WorkoutPlanExerciseResponse(val exerciseId: UUID, val name: String, val sets: Int, val minRepetitions: Int, val maxRepetitions: Int, val restSeconds: Int)
data class WorkoutPlanDayResponse(val name: String, val exercises: List<WorkoutPlanExerciseResponse>)
data class WorkoutPlanResponse(val id: UUID, val name: String, val days: List<WorkoutPlanDayResponse>, val active: Boolean = false)
data class SessionSetResponse(val exerciseName: String, val repetitions: Int, val loadKg: Double? = null, val exerciseId: UUID)
data class WorkoutSessionResponse(val id: UUID, val planName: String, val startedAt: String, val sets: List<SessionSetResponse>, val perceivedExertion: Int? = null, val note: String? = null)

@RestController
@RequestMapping("/api/v1/workout-plans")
class TrainingController(private val service: TrainingService) {
    @GetMapping fun list(@RequestAttribute("authenticatedUserId") userId: UUID) = service.listPlans(userId)
    @GetMapping("/archived") fun archived(@RequestAttribute("authenticatedUserId") userId: UUID) = service.listPlans(userId, true)
    @PostMapping fun create(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestBody request: CreateWorkoutPlanRequest) = ResponseEntity.status(HttpStatus.CREATED).body(IdResponse(service.createPlan(userId, request)))
    @PutMapping("/{planId}") fun update(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID, @RequestBody request: CreateWorkoutPlanRequest): ResponseEntity<Void> { service.updatePlan(userId, planId, request); return ResponseEntity.noContent().build() }
    @PutMapping("/{planId}/archive") fun archive(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID): ResponseEntity<Void> { service.archivePlan(userId, planId, true); return ResponseEntity.noContent().build() }
    @PutMapping("/{planId}/restore") fun restore(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID): ResponseEntity<Void> { service.archivePlan(userId, planId, false); return ResponseEntity.noContent().build() }
    @PutMapping("/{planId}/activate") fun activate(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID): ResponseEntity<Void> { service.activatePlan(userId, planId); return ResponseEntity.noContent().build() }
    @PostMapping("/{planId}/sessions") fun session(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID, @RequestBody request: CreateWorkoutSessionRequest) = ResponseEntity.status(HttpStatus.CREATED).body(IdResponse(service.createSession(userId, planId, request)))
    @ExceptionHandler(IllegalArgumentException::class) fun invalidRequest() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
    @ExceptionHandler(PlanAccessDeniedException::class) fun forbidden() = ResponseEntity.status(HttpStatus.FORBIDDEN).build<Void>()
}

@RestController
@RequestMapping("/api/v1/workout-sessions")
class WorkoutSessionHistoryController(private val service: TrainingService) {
    @GetMapping fun sessions(@RequestAttribute("authenticatedUserId") userId: UUID) = service.listSessions(userId)
    @PutMapping("/{sessionId}") fun update(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable sessionId: UUID, @RequestBody request: UpdateWorkoutSessionRequest): ResponseEntity<Void> { service.updateSession(userId, sessionId, request); return ResponseEntity.noContent().build() }
    @DeleteMapping("/{sessionId}") fun delete(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable sessionId: UUID): ResponseEntity<Void> { service.deleteSession(userId, sessionId); return ResponseEntity.noContent().build() }
    @ExceptionHandler(IllegalArgumentException::class) fun invalidRequest() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
    @ExceptionHandler(SessionNotFoundException::class) fun missing() = ResponseEntity.status(HttpStatus.NOT_FOUND).build<Void>()
}
