package com.angelhr28.yapechallenge.feature.detail

import com.angelhr28.yapechallenge.core.mvi.UiEffect
import com.angelhr28.yapechallenge.core.mvi.UiIntent
import com.angelhr28.yapechallenge.core.mvi.UiState
import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document

data class DetailState(
    val document: Document? = null,
    val decryptedBytes: ByteArray? = null,
    val accessLogs: List<AccessLog> = emptyList(),
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val currentLocation: String? = null,
    val error: String? = null
) : UiState {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DetailState) return false
        return document == other.document &&
                decryptedBytes.contentEquals(other.decryptedBytes) &&
                accessLogs == other.accessLogs &&
                isLoading == other.isLoading &&
                isAuthenticated == other.isAuthenticated &&
                currentLocation == other.currentLocation &&
                error == other.error
    }
    override fun hashCode(): Int {
        var result = document?.hashCode() ?: 0
        result = 31 * result + (decryptedBytes?.contentHashCode() ?: 0)
        result = 31 * result + accessLogs.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isAuthenticated.hashCode()
        result = 31 * result + (currentLocation?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}

private fun ByteArray?.contentHashCode(): Int {
    return this?.contentHashCode() ?: 0
}

sealed interface DetailIntent : UiIntent {
    data class LoadDocument(val documentId: Long) : DetailIntent
    data object OnAuthenticated : DetailIntent
    data object RequestDelete : DetailIntent
    data object ConfirmDelete : DetailIntent
    data class UpdateLocation(val location: String) : DetailIntent
}

sealed interface DetailEffect : UiEffect {
    data object RequestBiometricAuth : DetailEffect
    data object RequestDeleteBiometricAuth : DetailEffect
    data object NavigateBack : DetailEffect
    data class ShowError(val message: String) : DetailEffect
    data class ShowSuccess(val message: String) : DetailEffect
}
