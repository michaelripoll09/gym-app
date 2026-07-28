package com.gymapp.catalog

import com.gymapp.network.ExerciseResponse

data class ExerciseCatalogState(val loading: Boolean = true, val exercises: List<ExerciseResponse> = emptyList(), val error: String? = null)

sealed interface AppDestination { data object Access : AppDestination; data object Onboarding : AppDestination; data class Catalog(val profile: String) : AppDestination }

fun loadedCatalog(exercises: List<ExerciseResponse>) = ExerciseCatalogState(loading = false, exercises = exercises)
fun failedCatalog() = ExerciseCatalogState(loading = false, error = "No pudimos cargar el catálogo")
