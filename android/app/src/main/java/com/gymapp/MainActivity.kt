package com.gymapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gymapp.auth.AccessMode
import com.gymapp.auth.AccessState
import com.gymapp.auth.TokenStore
import com.gymapp.account.clearLocalAccountData
import com.gymapp.auth.accessErrorMessage
import com.gymapp.auth.passwordResetConfirmationError
import com.gymapp.auth.passwordResetRequestError
import com.gymapp.auth.requiresSessionReset
import com.gymapp.catalog.ExerciseCatalogScreen
import com.gymapp.catalog.ExerciseCatalogState
import com.gymapp.catalog.failedCatalog
import com.gymapp.catalog.loadedCatalog
import com.gymapp.calendar.TrainingCalendarScreen
import com.gymapp.calendar.TrainingCalendarState
import com.gymapp.calendar.calendarLoadError
import com.gymapp.curated.CuratedPlansScreen
import com.gymapp.curated.CuratedPlansState
import com.gymapp.guided.GuidedRoutineScreen
import com.gymapp.guided.GuidedRoutineDraft
import com.gymapp.guided.discardGuidedRoutine
import com.gymapp.network.CreateWorkoutPlanRequest
import com.gymapp.network.CreateWorkoutSessionRequest
import com.gymapp.network.ProgressMilestoneResponse
import com.gymapp.network.ExerciseSessionReferenceResponse
import com.gymapp.network.UpdateWorkoutSessionRequest
import com.gymapp.network.CuratedPlanResponse
import com.gymapp.network.GuidedRoutineProposalResponse
import com.gymapp.network.GymApi
import com.gymapp.network.RegisterRequest
import com.gymapp.network.TrainingProfileRequest
import com.gymapp.network.WorkoutDayRequest
import com.gymapp.network.WorkoutPlanExerciseRequest
import com.gymapp.network.WorkoutPlanResponse
import com.gymapp.network.SetLogRequest
import com.gymapp.network.BodyMeasurementRequest
import com.gymapp.network.BodyMeasurementResponse
import com.gymapp.measurements.BodyMeasurementsState
import com.gymapp.measurements.MeasurementsScreen
import com.gymapp.measurements.removeMeasurement
import com.gymapp.measurements.replaceMeasurement
import com.gymapp.goals.GoalsScreen
import com.gymapp.home.HomeDashboardScreen
import com.gymapp.network.ProgressGoalResponse
import com.gymapp.network.ChangePasswordRequest
import com.gymapp.network.PasswordResetConfirmationRequest
import com.gymapp.network.PasswordResetRequest
import com.gymapp.onboarding.TrainingProfile
import com.gymapp.onboarding.ProfileSelectionState
import com.gymapp.profile.ProfileRecoveryState
import com.gymapp.profile.ProfileEditorScreen
import com.gymapp.profile.ProfileEditorState
import com.gymapp.profile.offlineProfileFallback
import com.gymapp.profile.profileForUpdatedCatalog
import com.gymapp.profile.resolveProfileEditor
import com.gymapp.profile.resolveProfileRecovery
import com.gymapp.progress.TrainingProgressScreen
import com.gymapp.progress.TrainingProgressState
import com.gymapp.progress.PersonalRecordsState
import com.gymapp.progress.ProgressAnalysisState
import com.gymapp.offline.OfflineTrainingStore
import com.gymapp.offline.PendingSession
import com.gymapp.offline.PendingSessionsScreen
import com.gymapp.offline.enqueuePendingSession
import com.gymapp.offline.isOffline
import com.gymapp.offline.removeSyncedSession
import com.gymapp.routines.RoutineDraftState
import com.gymapp.routines.RoutineEditorScreen
import com.gymapp.routines.RoutineListScreen
import com.gymapp.sessions.SessionDraftState
import com.gymapp.sessions.SessionCorrectionDraftState
import com.gymapp.sessions.SessionHistoryScreen
import com.gymapp.sessions.SessionHistoryState
import com.gymapp.sessions.SessionScreen
import com.gymapp.sessions.SessionMutationRefreshState
import com.gymapp.sessions.refreshAfterSessionMutation
import com.gymapp.sessions.sessionReferencesLoadError
import com.gymapp.today.TodayTrainingScreen
import com.gymapp.today.TodayTrainingState
import com.gymapp.today.plansForToday
import com.gymapp.today.spanishDayName
import com.gymapp.today.todayLoadError
import com.gymapp.summary.WeeklySummaryScreen
import com.gymapp.summary.WeeklySummaryState
import com.gymapp.progression.ProgressionScreen
import com.gymapp.progression.ProgressionState
import com.gymapp.progression.RoutineReviewScreen
import com.gymapp.progression.RoutineReviewState
import com.gymapp.reminders.ReminderScheduler
import com.gymapp.reminders.ReminderScreen
import com.gymapp.reminders.ReminderSettings
import com.gymapp.reminders.ReminderStore
import com.gymapp.reminders.ReminderDestination
import com.gymapp.reminders.reminderDestination
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : ComponentActivity() {
    private var openToday by mutableStateOf(false)
    private var resetToken by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openToday = intent?.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_TODAY, false) == true
        resetToken = intent?.data?.getQueryParameter("token")
        setContent { AppFlow(TokenStore(this), openToday, resetToken, onTodayOpened = { openToday = false }) }
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); openToday = intent.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_TODAY, false); resetToken = intent.data?.getQueryParameter("token") }
}

@Composable
private fun AppFlow(store: TokenStore, openToday: Boolean = false, incomingResetToken: String? = null, onTodayOpened: () -> Unit = {}) {
    var token by remember { mutableStateOf(store.read()) }
    var requestingPasswordReset by remember { mutableStateOf(false) }
    var resetToken by remember(incomingResetToken) { mutableStateOf(incomingResetToken) }
    var recovery by remember { mutableStateOf<ProfileRecoveryState>(ProfileRecoveryState.Loading) }
    var recoveryAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(token, recoveryAttempt) {
        if (token != null) {
            recovery = ProfileRecoveryState.Loading
            val result = runCatching { GymApi.create().trainingProfile("Bearer ${token.orEmpty()}") }
            val resolved = resolveProfileRecovery(result.getOrNull(), (result.exceptionOrNull() as? HttpException)?.code())
            if (resolved is ProfileRecoveryState.Existing) store.saveProfile(resolved.primaryProfile)
            recovery = resolved
        }
    }
    when {
        token == null && resetToken != null -> PasswordResetConfirmation(resetToken.orEmpty(), onDone = { resetToken = null; requestingPasswordReset = false })
        token == null && requestingPasswordReset -> PasswordResetRequestScreen(onBack = { requestingPasswordReset = false })
        token == null -> Access(store, onRegistered = { token = it }, onLoggedIn = { token = it }, onForgotPassword = { requestingPasswordReset = true })
        recovery is ProfileRecoveryState.Loading -> ProfileLoading()
        recovery is ProfileRecoveryState.Existing -> TrainingHome(token.orEmpty(), (recovery as ProfileRecoveryState.Existing).primaryProfile, openToday, onTodayOpened, onUnauthorized = { store.clear(); token = null })
        recovery is ProfileRecoveryState.NeedsOnboarding -> Onboarding(token.orEmpty(), onSaved = { profile -> store.saveProfile(profile); recovery = ProfileRecoveryState.Existing(profile) }, onUnauthorized = { store.clear(); token = null })
        recovery is ProfileRecoveryState.Unauthorized -> LaunchedEffect(Unit) { store.clear(); token = null }
        recovery is ProfileRecoveryState.RetryableFailure && offlineProfileFallback(recovery, store.readProfile()) != null -> TrainingHome(token.orEmpty(), offlineProfileFallback(recovery, store.readProfile()).orEmpty(), openToday, onTodayOpened, onUnauthorized = { store.clear(); token = null })
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
private fun Access(store: TokenStore, onRegistered: (String) -> Unit, onLoggedIn: (String) -> Unit, onForgotPassword: () -> Unit) {
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
        if (state.mode == AccessMode.LOGIN) Button(onClick = onForgotPassword) { Text("Olvidé mi contraseña") }
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

@Composable
private fun PasswordResetRequestScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Restablecer contraseña")
        Text("Te enviaremos un enlace si existe una cuenta asociada al correo.")
        OutlinedTextField(email, { email = it }, label = { Text("Correo") })
        message?.let { Text(it, color = Color.Red) }
        Button(onClick = { scope.launch {
            val error = passwordResetRequestError(email)
            if (error != null) message = error else {
                runCatching { GymApi.create().requestPasswordReset(PasswordResetRequest(email)) }
                message = "Si existe una cuenta con este correo, recibirás un enlace de recuperación."
            }
        } }) { Text("Enviar enlace") }
        Button(onClick = onBack) { Text("Volver") }
    }
}

@Composable
private fun PasswordResetConfirmation(resetToken: String, onDone: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Crea una nueva contraseña")
        OutlinedTextField(password, { password = it }, label = { Text("Nueva contraseña") })
        OutlinedTextField(confirmation, { confirmation = it }, label = { Text("Confirmar contraseña") })
        message?.let { Text(it, color = Color.Red) }
        Button(onClick = { scope.launch {
            val error = passwordResetConfirmationError(password, confirmation)
            if (error != null) message = error else runCatching { GymApi.create().confirmPasswordReset(PasswordResetConfirmationRequest(resetToken, password)) }
                .onSuccess { onDone() }
                .onFailure { message = "El enlace es inválido o venció. Solicita uno nuevo." }
        } }) { Text("Guardar contraseña") }
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

private enum class TrainingScreen { HOME, CATALOG, GUIDED, CURATED, PROFILE, EDITOR, ROUTINES, TODAY, SESSION, HISTORY, PROGRESS, CALENDAR, MEASUREMENTS, GOALS, PROGRESSION, REVIEW, SUMMARY, REMINDERS, PENDING_SESSIONS }

@Composable
private fun TrainingHome(token: String, profile: String, openToday: Boolean, onTodayOpened: () -> Unit, onUnauthorized: () -> Unit) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    var screen by remember { mutableStateOf(TrainingScreen.HOME) }
    var activeProfile by remember { mutableStateOf(profile) }
    var catalog by remember { mutableStateOf(ExerciseCatalogState()) }
    var draft by remember { mutableStateOf(RoutineDraftState()) }
    var editingPlanId by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<WorkoutPlanResponse>>(emptyList()) }
    var plansLoading by remember { mutableStateOf(false) }
    var plansError by remember { mutableStateOf<String?>(null) }
    var plansOffline by remember { mutableStateOf(false) }
    var activatingPlanId by remember { mutableStateOf<String?>(null) }
    var refreshPlans by remember { mutableIntStateOf(0) }
    var archivedPlans by remember { mutableStateOf(false) }
    var pendingArchive by remember { mutableStateOf<WorkoutPlanResponse?>(null) }
    var today by remember { mutableStateOf(TodayTrainingState()) }
    var refreshToday by remember { mutableIntStateOf(0) }
    var session by remember { mutableStateOf<SessionDraftState?>(null) }
    var sessionReturnScreen by remember { mutableStateOf(TrainingScreen.ROUTINES) }
    var sessionSaving by remember { mutableStateOf(false) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    var sessionMilestones by remember { mutableStateOf<List<ProgressMilestoneResponse>?>(null) }
    var sessionReferences by remember { mutableStateOf<List<ExerciseSessionReferenceResponse>>(emptyList()) }
    var sessionReferencesLoading by remember { mutableStateOf(false) }
    var sessionReferencesError by remember { mutableStateOf<String?>(null) }
    val offlineStore = remember { OfflineTrainingStore(context) }
    var pendingSessions by remember { mutableStateOf(offlineStore.pendingSessions()) }
    var pendingSyncing by remember { mutableStateOf(false) }
    var pendingSyncMessage by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(SessionHistoryState()) }
    var refreshHistory by remember { mutableIntStateOf(0) }
    var sessionCorrection by remember { mutableStateOf<SessionCorrectionDraftState?>(null) }
    var sessionCorrectionSaving by remember { mutableStateOf(false) }
    var sessionCorrectionError by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(TrainingProgressState()) }
    var personalRecords by remember { mutableStateOf(PersonalRecordsState()) }
    var progressAnalysis by remember { mutableStateOf(ProgressAnalysisState()) }
    var refreshProgress by remember { mutableIntStateOf(0) }
    var calendarMonth by remember { mutableStateOf(YearMonth.now()) }
    var calendar by remember { mutableStateOf(TrainingCalendarState()) }
    var refreshCalendar by remember { mutableIntStateOf(0) }
    var calendarBackScreen by remember { mutableStateOf(TrainingScreen.HOME) }
    var measurements by remember { mutableStateOf(BodyMeasurementsState()) }
    var refreshMeasurements by remember { mutableIntStateOf(0) }
    var editingMeasurement by remember { mutableStateOf<BodyMeasurementResponse?>(null) }
    var measurementSaving by remember { mutableStateOf(false) }
    var measurementMessage by remember { mutableStateOf<String?>(null) }
    var goals by remember { mutableStateOf<List<ProgressGoalResponse>>(emptyList()) }
    var goalsLoading by remember { mutableStateOf(false) }
    var goalsError by remember { mutableStateOf<String?>(null) }
    var refreshGoals by remember { mutableIntStateOf(0) }
    var progression by remember { mutableStateOf(ProgressionState()) }
    var refreshProgression by remember { mutableIntStateOf(0) }
    var routineReview by remember { mutableStateOf(RoutineReviewState()) }
    var refreshRoutineReview by remember { mutableIntStateOf(0) }
    var weeklySummary by remember { mutableStateOf(WeeklySummaryState()) }
    var refreshWeeklySummary by remember { mutableIntStateOf(0) }
    var curatedPlans by remember { mutableStateOf(CuratedPlansState()) }
    var refreshCuratedPlans by remember { mutableIntStateOf(0) }
    var selectedCuratedPlan by remember { mutableStateOf<CuratedPlanResponse?>(null) }
    var adoptingCuratedPlan by remember { mutableStateOf(false) }
    var curatedPlanError by remember { mutableStateOf<String?>(null) }
    var guidedDraft by remember { mutableStateOf<GuidedRoutineDraft?>(null) }
    var guidedLoading by remember { mutableStateOf(false) }
    var guidedError by remember { mutableStateOf<String?>(null) }
    var guidedSaving by remember { mutableStateOf(false) }
    var refreshGuidedProposal by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<ProfileEditorState>(ProfileEditorState.Loading) }
    var editorAttempt by remember { mutableIntStateOf(0) }
    var editorSaving by remember { mutableStateOf(false) }
    var editorSaveError by remember { mutableStateOf<String?>(null) }
    var reminderSettings by remember { mutableStateOf(ReminderStore(context).readSettings()) }
    var notificationPermissionGranted by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notificationPermissionGranted = it }
    val scope = rememberCoroutineScope()
    fun loadSessionReferences(planId: String) {
        sessionReferences = emptyList()
        sessionReferencesError = null
        sessionReferencesLoading = true
        scope.launch {
            runCatching { GymApi.create().sessionReferences("Bearer $token", planId) }
                .onSuccess { sessionReferences = it }
                .onFailure { failure ->
                    if (requiresSessionReset((failure as? HttpException)?.code())) onUnauthorized()
                    else sessionReferencesError = sessionReferencesLoadError()
                }
            sessionReferencesLoading = false
        }
    }
    fun startTrainingSession(plan: WorkoutPlanResponse, destination: TrainingScreen, day: String? = null) {
        session = SessionDraftState.from(plan, day)
        sessionError = null
        sessionMilestones = null
        sessionReturnScreen = destination
        loadSessionReferences(plan.id)
        screen = TrainingScreen.SESSION
    }
    fun refreshSessionDependentScreens() {
        val refreshed = refreshAfterSessionMutation(SessionMutationRefreshState(refreshHistory, refreshProgress, refreshCalendar, refreshWeeklySummary))
        refreshHistory = refreshed.history
        refreshProgress = refreshed.progress
        refreshCalendar = refreshed.calendar
        refreshWeeklySummary = refreshed.weeklySummary
    }

    LaunchedEffect(openToday) { if (reminderDestination(openToday) == ReminderDestination.TODAY) { screen = TrainingScreen.TODAY; onTodayOpened() } }

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
        if (screen == TrainingScreen.ROUTINES || screen == TrainingScreen.REMINDERS) {
            plansLoading = true; plansError = null; plansOffline = false
            runCatching { if (archivedPlans) GymApi.create().archivedWorkoutPlans("Bearer $token") else GymApi.create().workoutPlans("Bearer $token") }.onSuccess {
                plans = it
                if (!archivedPlans) { offlineStore.cachePlans(it); ReminderScheduler.reschedule(context, it) }
            }.onFailure {
                if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                else if (!archivedPlans && isOffline(context) && offlineStore.cachedPlans().isNotEmpty()) { plans = offlineStore.cachedPlans(); plansOffline = true }
                else plansError = it.message ?: "No pudimos cargar tus rutinas"
            }
            plansLoading = false
        }
    }
    LaunchedEffect(screen, refreshToday) {
        if (screen == TrainingScreen.HOME || screen == TrainingScreen.TODAY) {
            today = today.copy(loading = true, error = null)
            val day = spanishDayName(LocalDate.now().dayOfWeek)
            runCatching { GymApi.create().workoutPlans("Bearer $token") }
                .onSuccess { plans = it; offlineStore.cachePlans(it); today = TodayTrainingState(loading = false, plans = plansForToday(it, day), hasActivePlan = it.any { plan -> plan.active }) }
                .onFailure {
                    if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                    else if (isOffline(context)) { val cached = offlineStore.cachedPlans(); today = TodayTrainingState(loading = false, plans = plansForToday(cached, day), hasActivePlan = cached.any { plan -> plan.active }) }
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
            personalRecords = PersonalRecordsState(loading = true)
            progressAnalysis = ProgressAnalysisState(loading = true)
            runCatching { GymApi.create().workoutSessions("Bearer $token") }
                .onSuccess { progress = TrainingProgressState.loaded(it, Instant.now()) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { progress = TrainingProgressState(error = "No pudimos cargar tu progreso") } }
            runCatching { GymApi.create().personalRecords("Bearer $token") }
                .onSuccess { personalRecords = PersonalRecordsState(records = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { personalRecords = PersonalRecordsState(error = "No pudimos cargar tus records") } }
            runCatching { GymApi.create().progressAnalysis("Bearer $token") }
                .onSuccess { progressAnalysis = ProgressAnalysisState(analysis = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { progressAnalysis = ProgressAnalysisState(error = "No pudimos cargar tu análisis") } }
        }
    }
    LaunchedEffect(screen, calendarMonth, refreshCalendar) {
        if (screen == TrainingScreen.CALENDAR) {
            calendar = calendar.copy(loading = true, error = null)
            runCatching {
                GymApi.create().trainingCalendar(
                    authorization = "Bearer $token",
                    month = calendarMonth.toString(),
                    zone = ZoneId.systemDefault().id,
                )
            }.onSuccess { days ->
                calendar = TrainingCalendarState(days = days, loading = false)
            }.onFailure {
                if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                else calendar = calendarLoadError(calendar.days)
            }
        }
    }
    LaunchedEffect(screen, refreshMeasurements) {
        if (screen == TrainingScreen.MEASUREMENTS) {
            measurements = BodyMeasurementsState(loading = true)
            runCatching { GymApi.create().bodyMeasurements("Bearer $token") }
                .onSuccess { measurements = BodyMeasurementsState(measurements = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else measurements = BodyMeasurementsState(error = "No pudimos cargar tus medidas. Reintenta.") }
        }
    }
    LaunchedEffect(screen, refreshGoals) { if (screen == TrainingScreen.HOME || screen == TrainingScreen.GOALS) { goalsLoading=true; goalsError=null; runCatching { GymApi.create().progressGoals("Bearer $token") }.onSuccess { goals=it }.onFailure { if(requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else goalsError="No pudimos cargar tus objetivos." }; goalsLoading=false } }
    LaunchedEffect(screen, refreshWeeklySummary) {
        if (screen == TrainingScreen.HOME || screen == TrainingScreen.SUMMARY) {
            weeklySummary = WeeklySummaryState(loading = true)
            runCatching { GymApi.create().weeklyTrainingSummary("Bearer $token") }
                .onSuccess { weeklySummary = WeeklySummaryState(summary = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else weeklySummary = WeeklySummaryState(error = "No pudimos cargar tu resumen semanal") }
        }
    }
    LaunchedEffect(screen, refreshProgression) { if (screen == TrainingScreen.PROGRESSION) { progression = ProgressionState(loading = true); runCatching { GymApi.create().progressionRecommendations("Bearer $token") }.onSuccess { progression = ProgressionState(items = it) }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else progression = ProgressionState(error = "No pudimos cargar tu progresión") } } }
    LaunchedEffect(screen, refreshRoutineReview, refreshPlans, refreshProgress) { if (screen == TrainingScreen.REVIEW) { routineReview = RoutineReviewState(loading = true); runCatching { GymApi.create().routineReview("Bearer $token") }.onSuccess { routineReview = RoutineReviewState(review = it) }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else routineReview = RoutineReviewState(error = "No pudimos cargar la revisión") } } }
    LaunchedEffect(screen, refreshCuratedPlans) {
        if (screen == TrainingScreen.CURATED) {
            curatedPlans = CuratedPlansState(loading = true)
            runCatching { GymApi.create().curatedPlans("Bearer $token") }
                .onSuccess { curatedPlans = CuratedPlansState(plans = it) }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else { curatedPlans = CuratedPlansState(error = "No pudimos cargar los planes recomendados") } }
        }
    }
    LaunchedEffect(screen, refreshGuidedProposal) {
        if (screen == TrainingScreen.GUIDED) {
            guidedLoading = true; guidedError = null; guidedDraft = null
            runCatching { GymApi.create().guidedRoutineProposal("Bearer $token") }
                .onSuccess { guidedDraft = GuidedRoutineDraft.from(it) }
                .onFailure {
                    if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                    else guidedError = "No pudimos generar una rutina compatible. Revisa tu perfil o intenta de nuevo."
                }
            guidedLoading = false
        }
    }

    when (screen) {
        TrainingScreen.HOME -> HomeDashboardScreen(
            today = today,
            day = spanishDayName(LocalDate.now().dayOfWeek),
            weeklySummary = weeklySummary,
            goals = goals,
            goalsLoading = goalsLoading,
            goalsError = goalsError,
            onStart = { plan -> startTrainingSession(plan, TrainingScreen.HOME, spanishDayName(LocalDate.now().dayOfWeek)) },
            onShowRoutines = { screen = TrainingScreen.ROUTINES },
            onShowCatalog = { screen = TrainingScreen.CATALOG },
            onShowProgress = { screen = TrainingScreen.PROGRESS },
            onShowCalendar = { calendarBackScreen = TrainingScreen.HOME; screen = TrainingScreen.CALENDAR },
            onShowReminders = { screen = TrainingScreen.REMINDERS },
            onRetryToday = { refreshToday++ },
            onRetrySummary = { refreshWeeklySummary++ },
            onRetryGoals = { refreshGoals++ },
        )
        TrainingScreen.CATALOG -> ExerciseCatalogScreen(catalog, onCreateRoutine = { screen = TrainingScreen.EDITOR }, onShowGuidedRoutine = { screen = TrainingScreen.GUIDED }, onShowCuratedPlans = { selectedCuratedPlan = null; curatedPlanError = null; screen = TrainingScreen.CURATED }, onShowRoutines = { screen = TrainingScreen.ROUTINES }, onShowToday = { screen = TrainingScreen.TODAY }, onShowSummary = { screen = TrainingScreen.SUMMARY }, onShowProfile = { screen = TrainingScreen.PROFILE })
        TrainingScreen.GUIDED -> GuidedRoutineScreen(guidedLoading, guidedDraft, catalog.exercises, guidedError, guidedSaving, onGenerate = { refreshGuidedProposal++ }, onDraftChanged = { guidedDraft = it }, onConfirm = { draft -> scope.launch {
            guidedSaving = true; guidedError = null
            runCatching { GymApi.create().createWorkoutPlan("Bearer $token", draft.toCreateWorkoutPlanRequest()) }
                .onSuccess { guidedDraft = null; refreshPlans++; screen = TrainingScreen.ROUTINES }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else guidedError = "No pudimos crear la rutina. Inténtalo de nuevo." }
            guidedSaving = false
        } }, onDiscard = { discardGuidedRoutine(); guidedDraft = null; screen = TrainingScreen.CATALOG }, onBack = { screen = TrainingScreen.CATALOG })
        TrainingScreen.CURATED -> CuratedPlansScreen(curatedPlans, selectedCuratedPlan, adoptingCuratedPlan, curatedPlanError, onSelect = { selectedCuratedPlan = it; curatedPlanError = null }, onAdopt = { plan -> scope.launch {
            adoptingCuratedPlan = true; curatedPlanError = null
            runCatching { GymApi.create().adoptCuratedPlan("Bearer $token", plan.id) }
                .onSuccess { selectedCuratedPlan = null; refreshPlans++; screen = TrainingScreen.ROUTINES }
                .onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else curatedPlanError = "No pudimos crear tu rutina" }
            adoptingCuratedPlan = false
        } }, onRetry = { refreshCuratedPlans++ }, onBack = { if (selectedCuratedPlan != null) selectedCuratedPlan = null else screen = TrainingScreen.CATALOG })
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
                onSaved = { updatedPrimary -> tokenStore.saveProfile(updatedPrimary); activeProfile = profileForUpdatedCatalog(updatedPrimary); screen = TrainingScreen.CATALOG },
                onLogout = { runCatching { clearLocalAccountData(onUnauthorized, offlineStore::clear, ReminderStore(context)::clear); true }.getOrDefault(false) },
                onChangePassword = { request: ChangePasswordRequest ->
                    val changed = runCatching { GymApi.create().changePassword("Bearer $token", request) }.isSuccess
                    if (changed) clearLocalAccountData(onUnauthorized, offlineStore::clear, ReminderStore(context)::clear)
                    changed
                },
                onDeleteAccount = {
                    val deleted = runCatching { GymApi.create().deleteAccount("Bearer $token") }.isSuccess
                    if (deleted) clearLocalAccountData(onUnauthorized, offlineStore::clear, ReminderStore(context)::clear)
                    deleted
                },
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
        TrainingScreen.ROUTINES -> RoutineListScreen(plans, plansLoading, plansError, archivedPlans, pendingArchive, activatingPlanId, plansOffline, pendingSessions.size, onStart = { plan ->
            startTrainingSession(plan, TrainingScreen.ROUTINES)
        }, onEdit = { plan -> draft = RoutineDraftState.from(plan); editingPlanId = plan.id; screen = TrainingScreen.EDITOR }, onArchive = { pendingArchive = it }, onConfirmArchive = { pendingArchive?.let { plan -> scope.launch { runCatching { GymApi.create().archiveWorkoutPlan("Bearer $token", plan.id) }.onSuccess { pendingArchive = null; refreshPlans++ }.onFailure { plansError = "No pudimos archivar la rutina" } } } }, onCancelArchive = { pendingArchive = null }, onActivate = { plan -> scope.launch { activatingPlanId = plan.id; plansError = null; runCatching { GymApi.create().activateWorkoutPlan("Bearer $token", plan.id) }.onSuccess { refreshPlans++ }.onFailure { if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized() else plansError = "No pudimos activar la rutina" }; activatingPlanId = null } }, onRestore = { plan -> scope.launch { runCatching { GymApi.create().restoreWorkoutPlan("Bearer $token", plan.id) }.onSuccess { refreshPlans++ }.onFailure { plansError = "No pudimos restaurar la rutina" } } }, onShowArchived = { archivedPlans = true; refreshPlans++ }, onShowActive = { archivedPlans = false; refreshPlans++ }, onHistory = { screen = TrainingScreen.HISTORY }, onProgress = { screen = TrainingScreen.PROGRESS }, onShowReminders = { screen = TrainingScreen.REMINDERS }, onShowPending = { pendingSyncMessage = null; screen = TrainingScreen.PENDING_SESSIONS }, onBack = { archivedPlans = false; screen = TrainingScreen.CATALOG })
        TrainingScreen.REMINDERS -> ReminderScreen(reminderSettings, notificationPermissionGranted, onSettingsChanged = { settings ->
            ReminderStore(context).saveSettings(settings); reminderSettings = settings; ReminderScheduler.reschedule(context, plans)
        }, onRequestPermission = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }, onOpenSettings = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
        }, onBack = { screen = TrainingScreen.ROUTINES })
        TrainingScreen.TODAY -> TodayTrainingScreen(today, spanishDayName(LocalDate.now().dayOfWeek), onStart = { plan ->
            startTrainingSession(plan, TrainingScreen.TODAY, spanishDayName(LocalDate.now().dayOfWeek))
        }, onShowRoutines = { screen = TrainingScreen.ROUTINES }, onRetry = { refreshToday++ }, onBack = { screen = TrainingScreen.CATALOG })
        TrainingScreen.SESSION -> session?.let { currentSession -> SessionScreen(currentSession, sessionSaving, sessionError, onRepetitionsChanged = { index, value ->
            session = session?.updateRepetitions(index, value)
        }, onLoadChanged = { index, value ->
            session = session?.updateLoadKg(index, value)
        }, onPerceivedExertionChanged = { value ->
            session = session?.updatePerceivedExertion(value)
        }, onNoteChanged = { value ->
            session = session?.updateNote(value)
        }, onFinish = {
            if (currentSession.validationMessage() == null) scope.launch {
                sessionSaving = true; sessionError = null
                val request = CreateWorkoutSessionRequest(
                    sets = currentSession.sets.map { SetLogRequest(it.exerciseId, it.repetitions.toInt(), it.loadKg.toDoubleOrNull()) },
                    perceivedExertion = currentSession.perceivedExertion.toIntOrNull(),
                    note = currentSession.note.trim().takeIf { it.isNotEmpty() },
                )
                runCatching { GymApi.create().createWorkoutSession("Bearer $token", currentSession.planId, request) }.onSuccess { response ->
                    refreshPlans++; refreshToday++; refreshWeeklySummary++; refreshHistory++; refreshProgress++
                    if (response.milestones.isEmpty()) screen = sessionReturnScreen else sessionMilestones = response.milestones
                }.onFailure {
                    if (requiresSessionReset((it as? HttpException)?.code())) onUnauthorized()
                    else if (isOffline(context)) {
                        val pending = PendingSession(UUID.randomUUID().toString(), currentSession.planId, currentSession.planName, Instant.now().toString(), request)
                        pendingSessions = enqueuePendingSession(pendingSessions, pending); offlineStore.savePendingSessions(pendingSessions)
                        pendingSyncMessage = "Sesión guardada sin conexión. Sincronízala cuando tengas internet."; screen = sessionReturnScreen
                    } else sessionError = it.message ?: "No fue posible guardar la sesión"
                }
                sessionSaving = false
            }
        }, onBack = { sessionMilestones = null; sessionReferences = emptyList(); sessionReferencesError = null; screen = sessionReturnScreen }, milestones = sessionMilestones, onMilestonesShown = { sessionMilestones = null; screen = sessionReturnScreen }, references = sessionReferences, referencesLoading = sessionReferencesLoading, referencesError = sessionReferencesError, onRetryReferences = { loadSessionReferences(currentSession.planId) }, onApplyReference = { exerciseId, reference -> session = session?.applyReference(exerciseId, reference) }) }
        TrainingScreen.PENDING_SESSIONS -> PendingSessionsScreen(pendingSessions, pendingSyncing, pendingSyncMessage, onSync = { scope.launch {
            pendingSyncing = true; var successful = 0; var failed = 0
            for (pending in pendingSessions.toList()) {
                val result = runCatching { GymApi.create().createWorkoutSession("Bearer $token", pending.planId, pending.request) }
                if (result.isSuccess) { pendingSessions = removeSyncedSession(pendingSessions, pending.localId); offlineStore.savePendingSessions(pendingSessions); successful++ }
                else if (requiresSessionReset((result.exceptionOrNull() as? HttpException)?.code())) { onUnauthorized(); break }
                else failed++
            }
            pendingSyncMessage = if (failed == 0) "$successful sesión(es) sincronizada(s)." else "Error: $successful sincronizada(s), $failed pendiente(s). Reintenta cuando tengas conexión."
            if (successful > 0) { refreshHistory++; refreshProgress++; refreshToday++; refreshWeeklySummary++ }
            pendingSyncing = false
        } }, onBack = { screen = TrainingScreen.ROUTINES })
        TrainingScreen.HISTORY -> SessionHistoryScreen(
            state = history,
            pending = pendingSessions,
            correction = sessionCorrection,
            savingCorrection = sessionCorrectionSaving,
            correctionError = sessionCorrectionError,
            onSelect = { history = history.select(it); sessionCorrection = null; sessionCorrectionError = null },
            onEdit = { selected -> sessionCorrection = SessionCorrectionDraftState.from(selected); sessionCorrectionError = null },
            onRepetitionsChanged = { index, value -> sessionCorrection = sessionCorrection?.updateRepetitions(index, value) },
            onLoadChanged = { index, value -> sessionCorrection = sessionCorrection?.updateLoadKg(index, value) },
            onEffortChanged = { value -> sessionCorrection = sessionCorrection?.updatePerceivedExertion(value) },
            onNoteChanged = { value -> sessionCorrection = sessionCorrection?.updateNote(value) },
            onSaveCorrection = {
                val correction = sessionCorrection
                if (correction != null && correction.validationMessage() == null) scope.launch {
                    sessionCorrectionSaving = true; sessionCorrectionError = null
                    val request = UpdateWorkoutSessionRequest(
                        sets = correction.sets.map { SetLogRequest(it.exerciseId, it.repetitions.toInt(), it.loadKg.toDoubleOrNull()) },
                        perceivedExertion = correction.perceivedExertion.toIntOrNull(),
                        note = correction.note.trim().takeIf { it.isNotEmpty() },
                    )
                    runCatching { GymApi.create().updateWorkoutSession("Bearer $token", correction.sessionId, request) }
                        .onSuccess { sessionCorrection = null; history = history.copy(selected = null); refreshSessionDependentScreens() }
                        .onFailure { error -> if (requiresSessionReset((error as? HttpException)?.code())) onUnauthorized() else sessionCorrectionError = "No pudimos guardar la correccion. Reintenta." }
                    sessionCorrectionSaving = false
                }
            },
            onCancelCorrection = { if (!sessionCorrectionSaving) { sessionCorrection = null; sessionCorrectionError = null } },
            onDelete = { selected -> scope.launch {
                sessionCorrectionSaving = true; sessionCorrectionError = null
                runCatching { GymApi.create().deleteWorkoutSession("Bearer $token", selected.id) }
                    .onSuccess { sessionCorrection = null; history = history.copy(selected = null); refreshSessionDependentScreens() }
                    .onFailure { error -> if (requiresSessionReset((error as? HttpException)?.code())) onUnauthorized() else sessionCorrectionError = "No pudimos eliminar la sesion. Reintenta." }
                sessionCorrectionSaving = false
            } },
            onRetry = { refreshHistory++ },
            onBack = {
                when {
                    sessionCorrection != null -> { sessionCorrection = null; sessionCorrectionError = null }
                    history.selected != null -> history = history.copy(selected = null)
                    else -> screen = TrainingScreen.ROUTINES
                }
            },
        )
        TrainingScreen.PROGRESS -> TrainingProgressScreen(progress, personalRecords, progressAnalysis, pendingSessions.size, onRetry = { refreshProgress++ }, onHistory = { screen = TrainingScreen.HISTORY }, onProgression = { screen = TrainingScreen.PROGRESSION }, onMeasurements = { editingMeasurement = null; measurementMessage = null; screen = TrainingScreen.MEASUREMENTS }, onGoals = { screen = TrainingScreen.GOALS }, onCalendar = { calendarBackScreen = TrainingScreen.PROGRESS; screen = TrainingScreen.CALENDAR }, onBack = { screen = TrainingScreen.ROUTINES })
        TrainingScreen.CALENDAR -> TrainingCalendarScreen(month = calendarMonth, state = calendar, onMonth = { calendarMonth = it }, onRetry = { refreshCalendar++ }, onBack = { screen = calendarBackScreen })
        TrainingScreen.GOALS -> GoalsScreen(goals, goalsLoading, goalsError, onSave = { request, id -> scope.launch { if(id==null) runCatching { GymApi.create().createProgressGoal("Bearer $token",request) }.onSuccess { goals=listOf(it)+goals; refreshProgress++ }.onFailure { goalsError="No pudimos guardar el objetivo." } else runCatching { GymApi.create().updateProgressGoal("Bearer $token",id,request) }.onSuccess { refreshGoals++; refreshProgress++ }.onFailure { goalsError="No pudimos actualizar el objetivo." } } }, onComplete = { goal -> scope.launch { runCatching { GymApi.create().completeProgressGoal("Bearer $token",goal.id) }.onSuccess { refreshGoals++; refreshProgress++ }.onFailure { goalsError="No pudimos completar el objetivo." } } }, onDelete = { goal -> scope.launch { runCatching { GymApi.create().deleteProgressGoal("Bearer $token",goal.id) }.onSuccess { goals=goals.filterNot { it.id==goal.id }; refreshProgress++ }.onFailure { goalsError="No pudimos eliminar el objetivo." } } }, onRetry = { refreshGoals++ }, onBack = { screen=TrainingScreen.PROGRESS })
        TrainingScreen.MEASUREMENTS -> MeasurementsScreen(
            state = measurements,
            saving = measurementSaving,
            editing = editingMeasurement,
            message = measurementMessage,
            onSave = { request -> scope.launch {
                measurementSaving = true; measurementMessage = null
                val editing = editingMeasurement
                val result = if (editing == null) runCatching { GymApi.create().createBodyMeasurement("Bearer $token", request) }
                else runCatching { GymApi.create().updateBodyMeasurement("Bearer $token", editing.id, request) }.map { editing.copy(recordedOn = request.recordedOn, weightKg = request.weightKg, waistCm = request.waistCm, hipCm = request.hipCm, chestCm = request.chestCm) }
                result.onSuccess { saved ->
                    measurements = if (editing == null) BodyMeasurementsState(measurements = (measurements.measurements + saved).sortedByDescending { it.recordedOn }) else BodyMeasurementsState(measurements = replaceMeasurement(measurements.measurements, saved))
                    editingMeasurement = null
                    refreshProgress++
                }.onFailure { error ->
                    if (requiresSessionReset((error as? HttpException)?.code())) onUnauthorized()
                    else measurementMessage = if ((error as? HttpException)?.code() == 422) "Revisa la fecha, los rangos y que no exista otra medida ese día." else "No pudimos guardar la medida. Reintenta."
                }
                measurementSaving = false
            } },
            onEdit = { editingMeasurement = it; measurementMessage = null },
            onDelete = { measurement -> scope.launch {
                measurementSaving = true; measurementMessage = null
                runCatching { GymApi.create().deleteBodyMeasurement("Bearer $token", measurement.id) }
                    .onSuccess { measurements = BodyMeasurementsState(measurements = removeMeasurement(measurements.measurements, measurement.id)); if (editingMeasurement?.id == measurement.id) editingMeasurement = null; refreshProgress++ }
                    .onFailure { error -> if (requiresSessionReset((error as? HttpException)?.code())) onUnauthorized() else measurementMessage = "No pudimos eliminar la medida. Reintenta." }
                measurementSaving = false
            } },
            onCancelEdit = { editingMeasurement = null; measurementMessage = null },
            onRetry = { refreshMeasurements++ },
            onBack = { editingMeasurement = null; screen = TrainingScreen.PROGRESS },
        )
        TrainingScreen.PROGRESSION -> ProgressionScreen(progression, onRetry = { refreshProgression++ }, onReview = { screen = TrainingScreen.REVIEW }, onBack = { screen = TrainingScreen.PROGRESS })
        TrainingScreen.REVIEW -> RoutineReviewScreen(routineReview, onRetry = { refreshRoutineReview++ }, onEdit = { id -> plans.firstOrNull { it.id == id }?.let { draft = RoutineDraftState.from(it); editingPlanId = it.id; screen = TrainingScreen.EDITOR } }, onBack = { screen = TrainingScreen.PROGRESS })
        TrainingScreen.SUMMARY -> WeeklySummaryScreen(weeklySummary, onRetry = { refreshWeeklySummary++ }, onBack = { screen = TrainingScreen.CATALOG })
    }
}
