package com.gymapp.profile

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gymapp.onboarding.ProfileSelectionState
import com.gymapp.onboarding.TrainingProfile
import com.gymapp.account.AccountDeletionState
import com.gymapp.account.accountDeletionResult
import com.gymapp.account.cancelAccountDeletion
import com.gymapp.account.confirmAccountDeletion
import com.gymapp.account.requestAccountDeletion
import kotlinx.coroutines.launch

@Composable
fun ProfileEditorScreen(
    initialSelection: ProfileSelectionState,
    saving: Boolean,
    saveError: String?,
    onSave: suspend (ProfileSelectionState) -> Boolean,
    onSaved: (String) -> Unit,
    onDeleteAccount: suspend () -> Boolean,
    onBack: () -> Unit,
) {
    var selection by remember(initialSelection) { mutableStateOf(initialSelection) }
    var error by remember { mutableStateOf<String?>(null) }
    var deletion by remember { mutableStateOf(AccountDeletionState()) }
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tu perfil de entrenamiento")
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
        (selection.validationError() ?: error ?: saveError)?.let { Text(it, color = Color.Red) }
        Button(onClick = {
            val request = selection.toRequest()
            if (request == null) { error = selection.validationError(); return@Button }
            error = null
            scope.launch { if (onSave(selection)) onSaved(request.primaryProfile) }
        }) { Text(if (saving) "Guardando…" else "Guardar cambios") }
        Button(onClick = onBack) { Text("Volver") }
        Text("Zona de privacidad")
        if (!deletion.confirming) Button(onClick = { deletion = requestAccountDeletion(deletion) }) { Text("Eliminar cuenta") }
        else {
            Text("Esta accion elimina tu cuenta y todos tus datos personales de forma irreversible.", color = Color.Red)
            deletion.error?.let { Text(it, color = Color.Red) }
            Button(onClick = { deletion = cancelAccountDeletion(deletion) }, enabled = !deletion.deleting) { Text("Cancelar") }
            Button(onClick = { scope.launch {
                deletion = confirmAccountDeletion(deletion)
                deletion = accountDeletionResult(deletion, onDeleteAccount())
            } }, enabled = !deletion.deleting) { Text(if (deletion.deleting) "Eliminando..." else "Confirmar eliminacion") }
        }
    }
}
