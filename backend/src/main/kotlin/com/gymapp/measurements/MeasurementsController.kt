package com.gymapp.measurements

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

data class BodyMeasurementRequest(
    val recordedOn: LocalDate,
    val weightKg: Double,
    val waistCm: Double? = null,
    val hipCm: Double? = null,
    val chestCm: Double? = null,
)

data class BodyMeasurementResponse(
    val id: UUID,
    val recordedOn: LocalDate,
    val weightKg: Double,
    val waistCm: Double? = null,
    val hipCm: Double? = null,
    val chestCm: Double? = null,
)

@RestController
@RequestMapping("/api/v1/body-measurements")
class MeasurementsController(private val service: MeasurementsService) {
    @GetMapping
    fun list(@RequestAttribute("authenticatedUserId") userId: UUID) = service.list(userId)

    @PostMapping
    fun create(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestBody request: BodyMeasurementRequest) =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, request))

    @PutMapping("/{measurementId}")
    fun update(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable measurementId: UUID, @RequestBody request: BodyMeasurementRequest): ResponseEntity<Void> {
        service.update(userId, measurementId, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{measurementId}")
    fun delete(@RequestAttribute("authenticatedUserId") userId: UUID, @PathVariable measurementId: UUID): ResponseEntity<Void> {
        service.delete(userId, measurementId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(InvalidBodyMeasurementException::class)
    fun invalidMeasurement() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()

    @ExceptionHandler(BodyMeasurementAccessDeniedException::class)
    fun accessDenied() = ResponseEntity.status(HttpStatus.FORBIDDEN).build<Void>()
}
