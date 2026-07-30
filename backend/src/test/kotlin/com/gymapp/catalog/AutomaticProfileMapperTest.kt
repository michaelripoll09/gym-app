package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AutomaticProfileMapperTest {
    @Test
    fun `maps bodyweight compound exercises to calisthenics and general fitness`() {
        val profiles = AutomaticProfileMapper.map(
            DatasetExerciseMetadata(
                name = "Push-up",
                category = "chest",
                equipment = "body weight",
                target = "pectorals",
            ),
        )

        assertEquals(
            setOf(TrainingProfileCode.CALISTHENICS, TrainingProfileCode.GENERAL_FITNESS),
            profiles,
        )
    }

    @Test
    fun `maps barbell squat to strength profiles`() {
        val profiles = AutomaticProfileMapper.map(
            DatasetExerciseMetadata(
                name = "Barbell squat",
                category = "upper legs",
                equipment = "barbell",
                target = "quads",
            ),
        )

        assertEquals(
            setOf(
                TrainingProfileCode.POWERLIFTING,
                TrainingProfileCode.BODYBUILDING,
                TrainingProfileCode.GENERAL_FITNESS,
            ),
            profiles,
        )
    }

    @Test
    fun `maps cardio running exercises to runner and crossfit profiles`() {
        val profiles = AutomaticProfileMapper.map(
            DatasetExerciseMetadata(
                name = "Running on treadmill",
                category = "cardio",
                equipment = "treadmill",
                target = "cardiovascular system",
            ),
        )

        assertEquals(
            setOf(
                TrainingProfileCode.RUNNING,
                TrainingProfileCode.CROSSFIT,
                TrainingProfileCode.GENERAL_FITNESS,
            ),
            profiles,
        )
    }

    @Test
    fun `maps resistance exercises to bodybuilding and general fitness`() {
        val profiles = AutomaticProfileMapper.map(
            DatasetExerciseMetadata(
                name = "Dumbbell curl",
                category = "upper arms",
                equipment = "dumbbell",
                target = "biceps",
            ),
        )

        assertEquals(
            setOf(TrainingProfileCode.BODYBUILDING, TrainingProfileCode.GENERAL_FITNESS),
            profiles,
        )
    }
}
