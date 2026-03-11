package com.angelhr28.yapechallenge.feature.detail

import com.angelhr28.yapechallenge.core.mvi.UiEffect
import com.angelhr28.yapechallenge.core.mvi.UiIntent
import com.angelhr28.yapechallenge.core.mvi.UiState
import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document

/**
 * Estado de la pantalla de detalle de documento.
 *
 * @property document Documento cargado actualmente.
 * @property decryptedBytes Bytes desencriptados del documento para su visualizacion.
 * @property accessLogs Historial de accesos al documento.
 * @property isLoading Indica si se esta cargando informacion.
 * @property isAuthenticated Indica si el usuario se autentico biometricamente.
 * @property currentLocation Ubicacion actual del usuario para la marca de agua.
 * @property error Mensaje de error, si existe.
 */
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
                decryptedBytes.nullSafeContentEquals(other.decryptedBytes) &&
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

private fun ByteArray?.nullSafeContentEquals(other: ByteArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}

/** Intenciones del usuario en la pantalla de detalle. */
sealed interface DetailIntent : UiIntent {
    /** Solicita cargar un documento por su [documentId]. */
    data class LoadDocument(val documentId: Long) : DetailIntent
    /** El usuario se autentico exitosamente. */
    data object OnAuthenticated : DetailIntent
    /** El usuario solicita eliminar el documento. */
    data object RequestDelete : DetailIntent
    /** Confirmacion de eliminacion tras autenticacion biometrica. */
    data object ConfirmDelete : DetailIntent
    /** Actualiza la ubicacion actual del usuario. */
    data class UpdateLocation(val location: String) : DetailIntent
}

/** Efectos secundarios emitidos desde la pantalla de detalle. */
sealed interface DetailEffect : UiEffect {
    /** Solicita autenticacion biometrica para ver el documento. */
    data object RequestBiometricAuth : DetailEffect
    /** Solicita autenticacion biometrica para eliminar el documento. */
    data object RequestDeleteBiometricAuth : DetailEffect
    /** Navega hacia atras. */
    data object NavigateBack : DetailEffect
    /** Muestra un mensaje de error. */
    data class ShowError(val message: String) : DetailEffect
    /** Muestra un mensaje de exito. */
    data class ShowSuccess(val message: String) : DetailEffect
}
