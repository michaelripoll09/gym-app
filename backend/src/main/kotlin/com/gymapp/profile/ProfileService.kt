package com.gymapp.profile

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProfileService(private val jdbc: JdbcTemplate) {
    @Transactional
    fun save(userId: UUID, request: TrainingProfileRequest): TrainingProfileRequest {
        ProfileRules.validate(request)
        val profileId = jdbc.query("select id from training_profiles where user_id = ?", { rs, _ -> UUID.fromString(rs.getString("id")) }, userId).firstOrNull()
            ?: UUID.randomUUID()
        jdbc.update(
            """insert into training_profiles (id, user_id, experience_level, primary_profile, goal, availability_band, available_days_per_week, session_duration_minutes)
               values (?, ?, ?, ?, ?, ?, ?, ?)
               on conflict (user_id) do update set experience_level = excluded.experience_level, primary_profile = excluded.primary_profile, goal = excluded.goal, availability_band = excluded.availability_band, available_days_per_week = excluded.available_days_per_week, session_duration_minutes = excluded.session_duration_minutes""",
            profileId, userId, request.experienceLevel.name, request.primaryProfile.name, request.goal, request.availabilityBand.name, request.availableDaysPerWeek, request.sessionDurationMinutes,
        )
        jdbc.update("delete from profile_secondary_interests where training_profile_id = ?", profileId)
        request.secondaryProfiles.forEach { jdbc.update("insert into profile_secondary_interests (training_profile_id, profile_code) values (?, ?)", profileId, it.name) }
        return request
    }

    fun get(userId: UUID): TrainingProfileRequest? {
        val row = jdbc.queryForList("select id, experience_level, primary_profile, goal, availability_band, available_days_per_week, session_duration_minutes from training_profiles where user_id = ?", userId).firstOrNull() ?: return null
        val id = row.getValue("id") as UUID
        val secondary = jdbc.query("select profile_code from profile_secondary_interests where training_profile_id = ? order by profile_code", { rs, _ -> TrainingProfileCode.valueOf(rs.getString("profile_code")) }, id)
        return TrainingProfileRequest(ExperienceLevel.valueOf(row.getValue("experience_level") as String), TrainingProfileCode.valueOf(row.getValue("primary_profile") as String), secondary, row.getValue("goal") as String, AvailabilityBand.valueOf(row.getValue("availability_band") as String), row.getValue("available_days_per_week") as Int, row.getValue("session_duration_minutes") as Int)
    }
}
