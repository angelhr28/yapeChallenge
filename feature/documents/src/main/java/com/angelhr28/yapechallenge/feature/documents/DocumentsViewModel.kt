package com.angelhr28.yapechallenge.feature.documents

import com.angelhr28.yapechallenge.core.mvi.MviViewModel
import com.angelhr28.yapechallenge.domain.usecase.AddDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentsUseCase
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel de la pantalla de listado de documentos.
 *
 * Gestiona la carga, filtrado y adicion de documentos.
 */
class DocumentsViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val addDocumentUseCase: AddDocumentUseCase
) : MviViewModel<DocumentsState, DocumentsIntent, DocumentsEffect>(DocumentsState()) {

    init {
        processIntent(DocumentsIntent.LoadDocuments)
    }

    override suspend fun handleIntent(intent: DocumentsIntent) {
        when (intent) {
            is DocumentsIntent.LoadDocuments -> loadDocuments()
            is DocumentsIntent.FilterByType -> filterDocuments(intent.type)
            is DocumentsIntent.AddDocument -> addDocument(intent.name, intent.mimeType, intent.bytes)
            is DocumentsIntent.OpenDocument -> sendEffect(DocumentsEffect.NavigateToDetail(intent.documentId))
        }
    }

    /** Carga los documentos aplicando el filtro actual. */
    private suspend fun loadDocuments() {
        setState { copy(isLoading = true, error = null) }
        try {
            getDocumentsUseCase(currentState.selectedFilter).collectLatest { documents ->
                setState { copy(documents = documents, isLoading = false) }
            }
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message) }
            sendEffect(DocumentsEffect.ShowError(e.message ?: "Error al cargar documentos"))
        }
    }

    /** Filtra los documentos por el tipo indicado y recarga la lista. */
    private suspend fun filterDocuments(type: com.angelhr28.yapechallenge.domain.model.DocumentType?) {
        setState { copy(selectedFilter = type, isLoading = true) }
        try {
            getDocumentsUseCase(type).collectLatest { documents ->
                setState { copy(documents = documents, isLoading = false) }
            }
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message) }
        }
    }

    /** Agrega un nuevo documento y recarga la lista tras el exito. */
    private suspend fun addDocument(name: String, mimeType: String, bytes: ByteArray) {
        setState { copy(isLoading = true) }
        try {
            addDocumentUseCase(name, mimeType, bytes)
            sendEffect(DocumentsEffect.ShowSuccess("Documento agregado exitosamente"))
            loadDocuments()
        } catch (e: Exception) {
            setState { copy(isLoading = false) }
            sendEffect(DocumentsEffect.ShowError(e.message ?: "Error al agregar documento"))
        }
    }
}
