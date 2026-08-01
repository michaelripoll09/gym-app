package com.gymapp.account

data class AccountDeletionState(
    val confirming: Boolean = false,
    val deleting: Boolean = false,
    val error: String? = null,
)

fun requestAccountDeletion(state: AccountDeletionState) = state.copy(confirming = true, error = null)
fun cancelAccountDeletion(state: AccountDeletionState) = state.copy(confirming = false, deleting = false, error = null)
fun confirmAccountDeletion(state: AccountDeletionState) = state.copy(deleting = true, error = null)
fun accountDeletionResult(state: AccountDeletionState, success: Boolean) = if (success) AccountDeletionState() else state.copy(deleting = false, error = "No pudimos eliminar tu cuenta. Reintenta.")
fun clearLocalAccountData(clearSession: () -> Unit, clearOfflineTraining: () -> Unit, clearReminders: () -> Unit) {
    clearSession()
    clearOfflineTraining()
    clearReminders()
}
