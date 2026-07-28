package com.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gymapp.auth.TokenStore
import com.gymapp.catalog.ExerciseCatalogScreen
import com.gymapp.catalog.ExerciseCatalogState
import com.gymapp.catalog.failedCatalog
import com.gymapp.catalog.loadedCatalog
import com.gymapp.network.CreateWorkoutPlanRequest
import com.gymapp.network.CreateWorkoutSessionRequest
import com.gymapp.network.GymApi
import com.gymapp.network.RegisterRequest
import com.gymapp.network.TrainingProfileRequest
import com.gymapp.network.WorkoutDayRequest
import com.gymapp.network.WorkoutPlanExerciseRequest
import com.gymapp.network.WorkoutPlanResponse
import com.gymapp.network.SetLogRequest
import com.gymapp.onboarding.TrainingProfile
import com.gymapp.routines.RoutineDraftState
import com.gymapp.routines.RoutineEditorScreen
import com.gymapp.routines.RoutineListScreen
import com.gymapp.sessions.SessionDraftState
import com.gymapp.sessions.SessionHistoryScreen
import com.gymapp.sessions.SessionHistoryState
import com.gymapp.sessions.SessionScreen
import java.time.Instant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppFlow(TokenStore(this)) }
    }
}

@Composable
private fun AppFlow(store: TokenStore) {
    var profile by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(store.read()) }
    when {
        token == null -> Access(store) { registeredToken -> token = registeredToken }
        profile == null -> Onboarding(token.orEmpty(), onSaved = { profile = TrainingProfile.GENERAL_FITNESS.name }, onFailed = { store.clear(); token = null })
        else -> TrainingHome(token.orEmpty(), profile!!)
    }
}

@Composable
private fun Access(store: TokenStore, onRegistered: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Crear cuenta")
        OutlinedTextField(email, { email = it }, label = { Text("Correo") })
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") })
        error?.let { Text(it, color = Color.Red) }
        Button(onClick = { scope.launch { runCatching { GymApi.create().register(RegisterRequest(email, password, Instant.now().toString())) }.onSuccess { store.save(it.accessToken); onRegistered(it.accessToken) }.onFailure { error = it.message ?: "No fue posible crear la cuenta" } } }) { Text("Crear cuenta") }
    }
}

internal suspend fun completeOnboarding(saveProfile: suspend () -> Unit) = runCatching { saveProfile() }.isSuccess

@Composable
private fun Onboarding(token: String, onSaved: () -> Unit, onFailed: () -> Unit) {
    LaunchedEffect(Unit) {
        if (completeOnboarding { GymApi.create().saveProfile("Bearer $token", TrainingProfileRequest("BEGINNER", TrainingProfile.GENERAL_FITNESS.name, emptyList(), "MUSCLE_GAIN", "MEDIUM", 3, 60)) }) onSaved() else onFailed()
    }
}

private enum class TrainingScreen { CATALOG, EDITOR, ROUTINES, SESSION, HISTORY }

@Composable
private fun TrainingHome(token: String, profile: String) {
    var screen by remember { mutableStateOf(TrainingScreen.CATALOG) }
    var catalog by remember { mutableStateOf(ExerciseCatalogState()) }
    var draft by remember { mutableStateOf(RoutineDraftState()) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<WorkoutPlanResponse>>(emptyList()) }
    var plansLoading by remember { mutableStateOf(false) }
    var plansError by remember { mutableStateOf<String?>(null) }
    var refreshPlans by remember { mutableIntStateOf(0) }
    var session by remember { mutableStateOf<SessionDraftState?>(null) }
    var sessionSaving by remember { mutableStateOf(false) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(SessionHistoryState()) }
    var refreshHistory by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile) { catalog = runCatching { loadedCatalog(GymApi.create().exercises(profile)) }.getOrElse { failedCatalog() } }
    LaunchedEffect(screen, refreshPlans) {
        if (screen == TrainingScreen.ROUTINES) {
            plansLoading = true; plansError = null
            runCatching { GymApi.create().workoutPlans("Bearer $token") }.onSuccess { plans = it }.onFailure { plansError = it.message ?: "No pudimos cargar tus rutinas" }
            plansLoading = false
        }
    }
    LaunchedEffect(screen, refreshHistory) {
        if (screen == TrainingScreen.HISTORY) {
            history = history.copy(loading = true, error = null, selected = null)
            runCatching { GymApi.create().workoutSessions("Bearer $token") }
                .onSuccess { history = SessionHistoryState(sessions = it) }
                .onFailure { history = history.copy(loading = false, error = it.message ?: "No pudimos cargar tu historial") }
        }
    }

    when (screen) {
        TrainingScreen.CATALOG -> ExerciseCatalogScreen(catalog, onCreateRoutine = { screen = TrainingScreen.EDITOR }, onShowRoutines = { screen = TrainingScreen.ROUTINES })
        TrainingScreen.EDITOR -> RoutineEditorScreen(draft, catalog.exercises, saving, saveError, onDraftChanged = { draft = it }, onSave = {
            if (draft.validationMessage() == null) scope.launch {
                saving = true; saveError = null
                val exercises = draft.exercises.map { WorkoutPlanExerciseRequest(it.exercise.id, it.sets, it.repetitions, it.repetitions, it.restSeconds) }
                val request = CreateWorkoutPlanRequest(draft.name.trim(), draft.scheduledDays.sorted().map { WorkoutDayRequest(it, exercises) })
                runCatching { GymApi.create().createWorkoutPlan("Bearer $token", request) }.onSuccess {
                    draft = RoutineDraftState(); refreshPlans++; screen = TrainingScreen.ROUTINES
                }.onFailure { saveError = it.message ?: "No fue posible guardar la rutina" }
                saving = false
            }
        }, onBack = { screen = TrainingScreen.CATALOG })
        TrainingScreen.ROUTINES -> RoutineListScreen(plans, plansLoading, plansError, onStart = { plan ->
            session = SessionDraftState.from(plan); sessionError = null; screen = TrainingScreen.SESSION
        }, onHistory = { screen = TrainingScreen.HISTORY }, onBack = { screen = TrainingScreen.CATALOG })
        TrainingScreen.SESSION -> session?.let { currentSession -> SessionScreen(currentSession, sessionSaving, sessionError, onStateChanged = { session = it }, onFinish = {
            if (currentSession.validationMessage() == null) scope.launch {
                sessionSaving = true; sessionError = null
                val request = CreateWorkoutSessionRequest(currentSession.sets.map { SetLogRequest(it.exerciseId, it.repetitions.toInt()) })
                runCatching { GymApi.create().createWorkoutSession("Bearer $token", currentSession.planId, request) }.onSuccess {
                    refreshPlans++; screen = TrainingScreen.ROUTINES
                }.onFailure { sessionError = it.message ?: "No fue posible guardar la sesión" }
                sessionSaving = false
            }
        }, onBack = { screen = TrainingScreen.ROUTINES }) }
        TrainingScreen.HISTORY -> SessionHistoryScreen(history, onSelect = { history = history.select(it) }, onRetry = { refreshHistory++ }, onBack = {
            if (history.selected != null) history = history.copy(selected = null) else screen = TrainingScreen.ROUTINES
        })
    }
}
