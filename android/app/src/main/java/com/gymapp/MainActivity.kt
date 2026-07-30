package com.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.gymapp.auth.AccessMode
import com.gymapp.auth.AccessState
import com.gymapp.auth.TokenStore
import com.gymapp.auth.accessErrorMessage
import com.gymapp.auth.requiresSessionReset
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
import com.gymapp.onboarding.ProfileSelectionState
import com.gymapp.profile.ProfileRecoveryState
import com.gymapp.profile.ProfileEditorScreen
import com.gymapp.profile.ProfileEditorState
import com.gymapp.profile.profileForUpdatedCatalog
import com.gymapp.profile.resolveProfileEditor
import com.gymapp.profile.resolveProfileRecovery
import com.gymapp.progress.TrainingProgressScreen
import com.gymapp.progress.TrainingProgressState
import com.gymapp.routines.RoutineDraftState
import com.gymapp.routines.RoutineEditorScreen
import com.gymapp.routines.RoutineListScreen
import com.gymapp.sessions.SessionDraftState
import com.gymapp.sessions.SessionHistoryScreen
import com.gymapp.sessions.SessionHistoryState
import com.gymapp.sessions.SessionScreen
import com.gymapp.today.TodayTrainingScreen
import com.gymapp.today.TodayTrainingState
import com.gymapp.today.plansForToday
import com.gymapp.today.spanishDayName
import com.gymapp.today.todayLoadError
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppFlow(TokenStore(this)) }
    }
}

@Composable
private fun AppFlow(store: TokenStore) {
    var token by remember { mutableStateOf(store.read()) }
    var recovery by remember { mutableStateOf<ProfileRecoveryState>(ProfileRecoveryState.Loading) }
    var recoveryAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(token, recoveryAttempt) {
        if (token != null) {
            recovery = ProfileRecoveryState.Loading
            val result = runCatching { GymApi.create().trainingProfile("Bearer ${token.orEmpty()}") }
            recovery = resolveProfileRecovery(result.getOrNull(), (result.exceptionOrNull() as? HttpException)?.code())
        }
    }
    when {
        token == null -> Access(store, onRegistered = { token = it }, onLoggedIn = { token = it })
        recovery is ProfileRecoveryState.Loading -> ProfileLoading()
        recovery is ProfileRecoveryState.Existing -> TrainingHome(token.orEmpty(), (recovery as ProfileRecoveryState.Existing).primaryProfile, onUnauthorized = { store.clear(); token = null })
        recovery is ProfileRecoveryState.NeedsOnboarding -> Onboarding(token.orEmpty(), onSaved = { profile -> recovery = ProfileRecoveryState.Existing(profile) }, onUnauthorized = { store.clear(); token = null })
        recovery is ProfileRecoveryState.Unauthorized -> LaunchedEffect(Unit) { store.clear(); token = null }
        recovery is ProfileRecoveryState.RetryableFailure -> ProfileRecoveryError { recoveryAttempt++ }
    }
}

@Composable
private fun ProfileLoading() {
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cargando tu perfil")
    }
}

@Composable
private fun ProfileRecoveryError(onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("No pudimos recuperar tu perfil", color = Color.Red)
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun Access(store: TokenStore, onRegistered: (String) -> Unit, onLoggedIn: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(AccessState()) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (state.mode == AccessMode.REGISTER) "Crear cuenta" else "Iniciar sesión")
        OutlinedTextField(email, { email = it }, label = { Text("Correo") })
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") })
        state.error?.let { Text(it, color = Color.Red) }
        Button(onClick = { state = state.toggleMode() }) { Text(if (state.mode == AccessMode.REGISTER) "Ya tengo cuenta" else "Crear una cuenta") }
        Button(onClick = { scope.launch {
            val mode = state.mode
            val response = runCatching { if (mode == AccessMode.REGISTER) GymApi.create().register(RegisterRequest(email, password, Instant.now().toString())) else GymApi.create().login(com.gymapp.network.LoginRequest(email, password)) }
            response.onSuccess {
                store.save(it.accessToken)
                if (mode == AccessMode.REGISTER) onRegistered(it.accessToken) else onLoggedIn(it.accessToken)
            }.onFailure { state = state.copy(error = accessErrorMessage(mode)) }
        } }) { Text(if (state.mode == AccessMode.REGISTER) "Crear cuenta" else "Iniciar sesión") }
    }
}

internal suspend fun completeOnboarding(saveProfile: suspend () -> Unit) = runCatching { saveProfile() }.isSuccess

@Composable
private fun Onboarding(token: String, onSaved: (String) -> Unit, onUnauthorized: () -> Unit) {
    var selection by remember { mutableStateOf(ProfileSelectionState()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Define tu perfil de entrenamiento")
        Text("Nivel de experiencia")
        listOf("BEGINNER" to "Principiante", "INTERMEDIATE" to "Intermedio", "ADVANCED" to "Avanzado").forEach { (value, label) ->
            Button(onClick = { selection = selection.copy(experienceLevel = value) }) { Text(if (selection.experienceLevel == value) "✓ $label" else label) }
        }
        Text("Disciplina principal")
        TrainingProfile.entries.forEach { profile ->
            Button(onClick = { selection = selection.copy(primary = profile, secondaryProfiles = selection.secondaryProfiles - profile, validationMessage = null) }) { Text(if (selection.primary == profile) "✓ ${profile.label}" else profile.label) }
        }
        Text("Intereses secundarios (opcional)")
        TrainingProfile.entries.filter { it != selection.primary }.forEach { profile ->
            Button(onClick = { selection = selection.toggleSecondary(profile) }) { Text(if (profile in selection.secondaryProfiles) "✓ ${profile.label}" else profile.label) }
        }
        Text("Objetivo")
        listOf("MUSCLE_GAIN" to "Ganar músculo", "FAT_LOSS" to "Perder grasa", "ENDURANCE" to "Resistencia", "SKILL" to "Técnica").forEach { (value, label) ->
            Button(onClick = { selection = selection.copy(goal = value) }) { Text(if (selection.goal == value) "✓ $label" else label) }
        }
        Text("Disponibilidad")
        OutlinedTextField(selection.days.toString(), { selection = selection.copy(days = it.toIntOrNull() ?: 0) }, label = { Text("Días por semana") })
        OutlinedTextField(selection.minutes.toString(), { selection = selection.copy(minutes = it.toIntOrNull() ?: 0) }, label = { Text("Minutos por sesión") })
        listOf("LOW" to "Baja", "MEDIUM" to "Media", "HIGH" to "Alta").forEach { (value, label) ->
            Button(onClick = { selection = selection.copy(availabilityBand = value) }) { Text(if (selection.availabilityBand == value) "✓ $label" else label) }
        }
        (selection.validationError() ?: selection.validationMessage ?: error)?.let { Text(it, color = Color.Red) }
        Button(onClick = { scope.launch {
            val request = selection.toRequest()
            if (request == null) { error = selection.validationError(); return@launch }
            saving = true; error = null
            val result = runCatching { GymApi.create().saveProfile("Bearer $token", request) }
            if (result.isSuccess) onSaved(request.primaryProfile)
            else if (requiresSessionReset((result.exceptionOrNull() as? HttpException)?.code())) onUnauthorized()
            else error = "No pudimos guardar tu perfil. Inténtalo de nuevo"
            saving = false
        } }) { Text(if (saving) "Guardando…" else "Continuar") }
    }
}

private enum class TrainingScreen { CATALOG, PROFILE, EDITOR, ROUTINES, TODAY, SESSION, HISTORY, PROGRESS }

@Composable
private fun TrainingHome(token: String, profile: String, onUnauthorized: () -> Unit) {
    var screen by remember { mutableStateOf(TrainingScreen.CATALOG) }
    var activeProfile by remember { mutableStateOf(profile) }
    var catalog by remember { mutableStateOf(ExerciseCatalogState()) }
    var draft by remember { mutableStateOf(RoutineDraftState()) }
    var editingPlanId by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<WorkoutPlanResponse>>(emptyList()) }
    var plansLoading by remember { mutableStateOf(false) }
    var plansError by remember { mutableStateOf<String?>(null) }
    var refreshPlans by remember { mutableIntStateOf(0) }
    var archivedPlans by remember { mutableStateOf(false) }
    var pendingArchive by remember { mutableStateOf<WorkoutPlanResponse?>(null) }
    var today by remember { mutableStateOf(TodayTrainingState()) }
    var refreshToday by remember { mutableIntStateOf(0) }
    var session by remember { mutableStateOf<SessionDraftState?>(null) }
    var sessionReturnScreen by remember { mutableStateOf(TrainingScreen.ROUTINES) }
    var sessionSaving by remember { mutableStateOf(false) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(SessionHistoryState()) }
    var refreshHistory by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf(TrainingProgressState()) }
    var refreshProgress by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<ProfileEditorState>(ProfileEditorState.Loading) }
    var editorAttempt by remember { mutableIntStateOf(0) }
    var editorSaving by remember { mutableStateOf(false) }
    var editorSaveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activeProfile) {
        runCatching { GymApi.create().exercises(activeProfile) }
            .onSuccess { catalog = loadedCatalog(it) }
            .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { catalog = failedCatalog() } }
    }
    LaunchedEffect(screen, editorAttempt) {
        if (screen == TrainingScreen.PROFILE) {
            editor = ProfileEditorState.Loading
            val result = runCatching { GymApi.create().trainingProfile("Bearer $token") }
            editor = resolveProfileEditor(result.getOrNull(), (result.exceptionOrNull() as? HttpException)?.code())
        }
    }
    LaunchedEffect(screen, refreshPlans) {
        if (screen == TrainingScreen.ROUTINES) {
            plansLoading = true; plansError = null
            runCatching { if (archivedPlans) GymApi.create().archivedWorkoutPlans("Bearer $token") else GymApi.create().workoutPlans("Bearer $token") }.onSuccess { plans = it }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { plansError = it.message ?: "No pudimos cargar tus rutinas" } }
            plansLoading = false
        }
    }
    LaunchedEffect(screen, refreshToday) {
        if (screen == TrainingScreen.TODAY) {
            today = today.copy(loading = true, error = null)
            val day = spanishDayName(LocalDate.now().dayOfWeek)
            runCatching { GymApi.create().workoutPlans("Bearer $token") }
                .onSuccess { today = TodayTrainingState(loading = false, plans = plansForToday(it, day)) }
                .onFailure {
                    if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                    else today = todayLoadError(today.plans)
                }
        }
    }
    LaunchedEffect(screen, refreshHistory) {
        if (screen == TrainingScreen.HISTORY) {
            history = history.copy(loading = true, error = null, selected = null)
            runCatching { GymApi.create().workoutSessions("Bearer $token") }
                .onSuccess { history = SessionHistoryState(sessions = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { history = history.copy(loading = false, error = it.message ?: "No pudimos cargar tu historial") } }
        }
    }
    LaunchedEffect(screen, refreshProgress) {
        if (screen == TrainingScreen.PROGRESS) {
            progress = TrainingProgressState(loading = true)
            runCatching { GymApi.create().workoutSessions("Bearer $token") }
                .onSuccess { progress = TrainingProgressState.loaded(it, Instant.now()) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { progress = TrainingProgressState(error = "No pudimos cargar tu progreso") } }
        }
    }

    when (screen) {
        TrainingScreen.CATALOG -> ExerciseCatalogScreen(catalog, onCreateRoutine = { screen = TrainingScreen.EDITOR }, onShowRoutines = { screen = TrainingScreen.ROUTINES }, onShowToday = { screen = TrainingScreen.TODAY }, onShowProfile = { screen = TrainingScreen.PROFILE })
        TrainingScreen.PROFILE -> when (val currentEditor = editor) {
            ProfileEditorState.Loading -> ProfileLoading()
            ProfileEditorState.Unauthorized -> LaunchedEffect(Unit) { onUnauthorized() }
            ProfileEditorState.RetryableFailure -> ProfileRecoveryError { editorAttempt++ }
            is ProfileEditorState.Editing -> ProfileEditorScreen(
                initialSelection = currentEditor.selection,
                saving = editorSaving,
                saveError = editorSaveError,
                onSave = { selection ->
                    val request = selection.toRequest() ?: return@ProfileEditorScreen false
                    editorSaving = true; editorSaveError = null
                    val result = runCatching { GymApi.create().saveProfile("Bearer $token", request) }
                    editorSaving = false
                    if (result.isSuccess) true
                    else if (requiresSessionReset((result.exceptionOrNull() as? HttpException)?.code())) { onUnauthorized(); false }
                    else { editorSaveError = "No pudimos guardar tu perfil. Inténtalo de nuevo"; false }
                },
                onSaved = { updatedPrimary -> activeProfile = profileForUpdatedCatalog(updatedPrimary); screen = TrainingScreen.CATALOG },
                onBack = { screen = TrainingScreen.CATALOG },
            )
        }
        TrainingScreen.EDITOR -> RoutineEditorScreen(draft, catalog.exercises, saving, saveError, onDraftChanged = { draft = it }, onSave = {
            if (draft.validationMessage() == null) scope.launch {
                saving = true; saveError = null
                val exercises = draft.exercises.map { WorkoutPlanExerciseRequest(it.exercise.id, it.sets, it.repetitions, it.repetitions, it.restSeconds) }
                val request = CreateWorkoutPlanRequest(draft.name.trim(), draft.scheduledDays.sorted().map { WorkoutDayRequest(it, exercises) })
                val result = if (editingPlanId == null) runCatching { GymApi.create().createWorkoutPlan("Bearer $token", request) }.map { Unit }
                else runCatching { GymApi.create().updateWorkoutPlan("Bearer $token", editingPlanId.orEmpty(), request) }
                result.onSuccess {
                    draft = RoutineDraftState(); editingPlanId = null; refreshPlans++; screen = TrainingScreen.ROUTINES
                }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { saveError = it.message ?: "No fue posible guardar la rutina" } }
                saving = false
            }
        }, onBack = { draft = RoutineDraftState(); editingPlanId = null; screen = TrainingScreen.CATALOG })
        TrainingScreen.ROUTINES -> RoutineListScreen(plans, plansLoading, plansError, archivedPlans, pendingArchive, onStart = { plan ->
            session = SessionDraftState.from(plan); sessionError = null; sessionReturnScreen = TrainingScreen.ROUTINES; screen = TrainingScreen.SESSION
        }, onEdit = { plan -> draft = RoutineDraftState.from(plan); editingPlanId = plan.id; screen = TrainingScreen.EDITOR }, onArchive = { pendingArchive = it }, onConfirmArchive = { pendingArchive?.let { plan -> scope.launch { runCatching { GymApi.create().archiveWorkoutPlan("Bearer $token", plan.id) }.onSuccess { pendingArchive = null; refreshPlans++ }.onFailure { plansError = "No pudimos archivar la rutina" } } } }, onCancelArchive = { pendingArchive = null }, onRestore = { plan -> scope.launch { runCatching { GymApi.create().restoreWorkoutPlan("Bearer $token", plan.id) }.onSuccess { refreshPlans++ }.onFailure { plansError = "No pudimos restaurar la rutina" } } }, onShowArchived = { archivedPlans = true; refreshPlans++ }, onShowActive = { archivedPlans = false; refreshPlans++ }, onHistory = { screen = TrainingScreen.HISTORY }, onProgress = { screen = TrainingScreen.PROGRESS }, onBack = { archivedPlans = false; screen = TrainingScreen.CATALOG })
        TrainingScreen.TODAY -> TodayTrainingScreen(today, spanishDayName(LocalDate.now().dayOfWeek), onStart = { plan ->
            session = SessionDraftState.from(plan, spanishDayName(LocalDate.now().dayOfWeek)); sessionError = null; sessionReturnScreen = TrainingScreen.TODAY; screen = TrainingScreen.SESSION
        }, onShowRoutines = { screen = TrainingScreen.ROUTINES }, onRetry = { refreshToday++ }, onBack = { screen = TrainingScreen.CATALOG })
        TrainingScreen.SESSION -> session?.let { currentSession -> SessionScreen(currentSession, sessionSaving, sessionError, onStateChanged = { session = it }, onFinish = {
            if (currentSession.validationMessage() == null) scope.launch {
                sessionSaving = true; sessionError = null
                val request = CreateWorkoutSessionRequest(currentSession.sets.map { SetLogRequest(it.exerciseId, it.repetitions.toInt(), it.loadKg.toDoubleOrNull()) })
                runCatching { GymApi.create().createWorkoutSession("Bearer $token", currentSession.planId, request) }.onSuccess {
                    refreshPlans++; refreshToday++; screen = sessionReturnScreen
                }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { sessionError = it.message ?: "No fue posible guardar la sesión" } }
                sessionSaving = false
            }
        }, onBack = { screen = sessionReturnScreen }) }
        TrainingScreen.HISTORY -> SessionHistoryScreen(history, onSelect = { history = history.select(it) }, onRetry = { refreshHistory++ }, onBack = {
            if (history.selected != null) history = history.copy(selected = null) else screen = TrainingScreen.ROUTINES
        })
        TrainingScreen.PROGRESS -> TrainingProgressScreen(progress, onRetry = { refreshProgress++ }, onHistory = { screen = TrainingScreen.HISTORY }, onBack = { screen = TrainingScreen.ROUTINES })
    }
}
