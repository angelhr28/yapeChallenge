package com.angelhr28.yapechallenge.feature.detail

import com.angelhr28.yapechallenge.core.mvi.MviViewModel
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.usecase.DeleteDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetAccessLogsUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDecryptedDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentDetailUseCase
import com.angelhr28.yapechallenge.domain.usecase.LogAccessUseCase
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel de la pantalla de detalle de documento.
 *
 * Gestiona la carga, desencriptacion, registro de accesos y eliminacion de documentos.
 */
class DetailViewModel(
    private val getDocumentDetailUseCase: GetDocumentDetailUseCase,
    private val getDecryptedDocumentUseCase: GetDecryptedDocumentUseCase,
    private val getAccessLogsUseCase: GetAccessLogsUseCase,
    private val logAccessUseCase: LogAccessUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : MviViewModel<DetailState, DetailIntent, DetailEffect>(DetailState()) {

    override suspend fun handleIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadDocument -> loadDocument(intent.documentId)
            is DetailIntent.OnAuthenticated -> onAuthenticated()
            is DetailIntent.RequestDelete -> sendEffect(DetailEffect.RequestDeleteBiometricAuth)
            is DetailIntent.ConfirmDelete -> deleteDocument()
            is DetailIntent.UpdateLocation -> setState { copy(currentLocation = intent.location) }
        }
    }

    /** Carga el documento y solicita autenticacion biometrica. */
    private suspend fun loadDocument(documentId: Long) {
        setState { copy(isLoading = true, error = null) }
        try {
            val document = getDocumentDetailUseCase(documentId)
            if (document != null) {
                setState { copy(document = document, isLoading = false) }
                sendEffect(DetailEffect.RequestBiometricAuth)
                getAccessLogsUseCase(documentId).collectLatest { logs ->
                    setState { copy(accessLogs = logs) }
                }
            } else {
                setState { copy(isLoading = false, error = "Documento no encontrado") }
                sendEffect(DetailEffect.ShowError("Documento no encontrado"))
            }
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message) }
            sendEffect(DetailEffect.ShowError(e.message ?: "Error al cargar documento"))
        }
    }

    /** Desencripta el documento y registra el acceso tras autenticacion exitosa. */
    private suspend fun onAuthenticated() {
        setState { copy(isAuthenticated = true) }
        val document = currentState.document ?: return
        try {
            val bytes = getDecryptedDocumentUseCase(document)
            setState { copy(decryptedBytes = bytes) }
            logAccessUseCase(document.id, AccessAction.VIEW, currentState.currentLocation)
        } catch (e: Exception) {
            sendEffect(DetailEffect.ShowError("Error al desencriptar: ${e.message}"))
        }
    }

    /** Registra el acceso de eliminacion y elimina el documento. */
    private suspend fun deleteDocument() {
        val document = currentState.document ?: return
        try {
            logAccessUseCase(document.id, AccessAction.DELETE, currentState.currentLocation)
            deleteDocumentUseCase(document.id)
            sendEffect(DetailEffect.ShowSuccess("Documento eliminado"))
            sendEffect(DetailEffect.NavigateBack)
        } catch (e: Exception) {
            sendEffect(DetailEffect.ShowError(e.message ?: "Error al eliminar"))
        }
    }
}
