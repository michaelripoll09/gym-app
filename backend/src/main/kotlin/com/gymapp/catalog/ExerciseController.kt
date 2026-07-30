package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class PublishedExerciseResponse(val id: String, val name: String, val spanishInstructions: String)

@RestController
@RequestMapping("/api/v1/exercises")
class ExerciseController(private val jdbc: JdbcTemplate) {
    @GetMapping
    fun list(@RequestParam profile: TrainingProfileCode): List<PublishedExerciseResponse> = jdbc.query(
        """select e.id, e.name, e.spanish_instructions from exercises e
           join exercise_training_profiles p on p.exercise_id = e.id
           where e.published = true and p.profile_code = ? order by e.name""",
        { rs, _ -> PublishedExerciseResponse(rs.getString("id"), rs.getString("name"), rs.getString("spanish_instructions")) },
        profile.name,
    )
}
