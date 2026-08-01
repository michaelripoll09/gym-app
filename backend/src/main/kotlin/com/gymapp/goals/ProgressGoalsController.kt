package com.gymapp.goals

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

data class ProgressGoalRequest(val type: String, val targetValue: Double, val targetDate: LocalDate? = null, val exerciseName: String? = null)
data class ProgressGoalResponse(val id: UUID, val type: String, val targetValue: Double, val targetDate: LocalDate?, val status: String, val currentValue: Double?, val exerciseName: String?, val completedAt: String?)
@RestController @RequestMapping("/api/v1/progress-goals")
class ProgressGoalsController(private val service: ProgressGoalsService) {
    @GetMapping fun list(@RequestAttribute("authenticatedUserId") userId: UUID) = service.list(userId)
    @PostMapping fun create(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestBody request: ProgressGoalRequest) = ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, request))
    @PutMapping("/{id}") fun update(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable id: UUID, @RequestBody request: ProgressGoalRequest): ResponseEntity<Void> { service.update(userId,id,request); return ResponseEntity.noContent().build() }
    @PutMapping("/{id}/complete") fun complete(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable id: UUID): ResponseEntity<Void> { service.complete(userId,id); return ResponseEntity.noContent().build() }
    @DeleteMapping("/{id}") fun delete(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable id: UUID): ResponseEntity<Void> { service.delete(userId,id); return ResponseEntity.noContent().build() }
    @ExceptionHandler(InvalidProgressGoalException::class) fun invalid() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
    @ExceptionHandler(ProgressGoalAccessDeniedException::class) fun denied() = ResponseEntity.status(HttpStatus.FORBIDDEN).build<Void>()
}
