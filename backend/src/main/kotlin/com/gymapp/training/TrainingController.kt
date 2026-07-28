package com.gymapp.training

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

data class ExercisePlanRequest(val exerciseId: UUID, val sets: Int, val minRepetitions: Int, val maxRepetitions: Int)
data class WorkoutDayRequest(val name: String, val exercises: List<ExercisePlanRequest>)
data class CreateWorkoutPlanRequest(val name: String, val days: List<WorkoutDayRequest>)
data class SetLogRequest(val exerciseId: UUID, val repetitions: Int)
data class CreateWorkoutSessionRequest(val sets: List<SetLogRequest>)
data class IdResponse(val id: UUID)

@RestController
@RequestMapping("/api/v1/workout-plans")
class TrainingController(private val service: TrainingService) {
    @PostMapping fun create(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestBody request: CreateWorkoutPlanRequest) = ResponseEntity.status(HttpStatus.CREATED).body(IdResponse(service.createPlan(userId, request)))
    @PostMapping("/{planId}/sessions") fun session(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable planId: UUID, @RequestBody request: CreateWorkoutSessionRequest) = ResponseEntity.status(HttpStatus.CREATED).body(IdResponse(service.createSession(userId, planId, request)))
    @ExceptionHandler(IllegalArgumentException::class) fun invalidRequest() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
    @ExceptionHandler(PlanAccessDeniedException::class) fun forbidden() = ResponseEntity.status(HttpStatus.FORBIDDEN).build<Void>()
}
