package com.gymapp.measurements

import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

class InvalidBodyMeasurementException : RuntimeException()
class BodyMeasurementAccessDeniedException : RuntimeException()

@Service
class MeasurementsService(private val jdbc: JdbcTemplate) {
    fun list(userId: UUID): List<BodyMeasurementResponse> = jdbc.query(
        "select id, recorded_on, weight_kg, waist_cm, hip_cm, chest_cm from body_measurements where user_id=? order by recorded_on desc",
        { row, _ -> row.toResponse() }, userId,
    )

    @Transactional
    fun create(userId: UUID, request: BodyMeasurementRequest): BodyMeasurementResponse {
        validate(request)
        val id = UUID.randomUUID()
        try {
            jdbc.update(
                "insert into body_measurements (id, user_id, recorded_on, weight_kg, waist_cm, hip_cm, chest_cm) values (?, ?, ?, ?, ?, ?, ?)",
                id, userId, request.recordedOn, request.weightKg, request.waistCm, request.hipCm, request.chestCm,
            )
        } catch (_: DuplicateKeyException) {
            throw InvalidBodyMeasurementException()
        }
        return BodyMeasurementResponse(id, request.recordedOn, request.weightKg, request.waistCm, request.hipCm, request.chestCm)
    }

    @Transactional
    fun update(userId: UUID, measurementId: UUID, request: BodyMeasurementRequest) {
        validate(request)
        try {
            if (jdbc.update(
                    "update body_measurements set recorded_on=?, weight_kg=?, waist_cm=?, hip_cm=?, chest_cm=? where id=? and user_id=?",
                    request.recordedOn, request.weightKg, request.waistCm, request.hipCm, request.chestCm, measurementId, userId,
                ) != 1
            ) throw BodyMeasurementAccessDeniedException()
        } catch (_: DuplicateKeyException) {
            throw InvalidBodyMeasurementException()
        }
    }

    @Transactional
    fun delete(userId: UUID, measurementId: UUID) {
        if (jdbc.update("delete from body_measurements where id=? and user_id=?", measurementId, userId) != 1) throw BodyMeasurementAccessDeniedException()
    }

    private fun validate(request: BodyMeasurementRequest) {
        if (request.recordedOn.isAfter(LocalDate.now()) || request.weightKg !in 20.0..500.0 || listOfNotNull(request.waistCm, request.hipCm, request.chestCm).any { it !in 20.0..250.0 }) {
            throw InvalidBodyMeasurementException()
        }
    }
}

private fun java.sql.ResultSet.toResponse() = BodyMeasurementResponse(
    id = getObject("id", UUID::class.java),
    recordedOn = getObject("recorded_on", LocalDate::class.java),
    weightKg = getDouble("weight_kg"),
    waistCm = (getObject("waist_cm") as? Number)?.toDouble(),
    hipCm = (getObject("hip_cm") as? Number)?.toDouble(),
    chestCm = (getObject("chest_cm") as? Number)?.toDouble(),
)
