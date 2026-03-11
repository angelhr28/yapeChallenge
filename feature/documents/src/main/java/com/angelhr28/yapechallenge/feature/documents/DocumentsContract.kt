package com.angelhr28.yapechallenge.feature.documents

import com.angelhr28.yapechallenge.core.mvi.UiEffect
import com.angelhr28.yapechallenge.core.mvi.UiIntent
import com.angelhr28.yapechallenge.core.mvi.UiState
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType

/**
 * Estado de la pantalla de listado de documentos.
 *
 * @property documents Lista de documentos disponibles.
 * @property isLoading Indica si se estan cargando los documentos.
 * @property selectedFilter Filtro de tipo de documento seleccionado, nulo si se muestran todos.
 * @property error Mensaje de error, si existe.
 */
data class DocumentsState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFilter: DocumentType? = null,
    val error: String? = null
) : UiState

/** Intenciones del usuario en la pantalla de documentos. */
sealed interface DocumentsIntent : UiIntent {
    /** Solicita cargar la lista de documentos. */
    data object LoadDocuments : DocumentsIntent
    /** Aplica un filtro por tipo de documento. */
    data class FilterByType(val type: DocumentType?) : DocumentsIntent
    /** Agrega un nuevo documento con su nombre, tipo MIME y bytes del archivo. */
    data class AddDocument(val name: String, val mimeType: String, val bytes: ByteArray) : DocumentsIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AddDocument) return false
            return name == other.name && mimeType == other.mimeType && bytes.contentEquals(other.bytes)
        }
        override fun hashCode(): Int = 31 * (31 * name.hashCode() + mimeType.hashCode()) + bytes.contentHashCode()
    }
    /** Abre el detalle de un documento por su identificador. */
    data class OpenDocument(val documentId: Long) : DocumentsIntent
}

/** Efectos secundarios emitidos desde la pantalla de documentos. */
sealed interface DocumentsEffect : UiEffect {
    /** Navega a la pantalla de detalle del documento indicado. */
    data class NavigateToDetail(val documentId: Long) : DocumentsEffect
    /** Muestra un mensaje de error. */
    data class ShowError(val message: String) : DocumentsEffect
    /** Muestra un mensaje de exito. */
    data class ShowSuccess(val message: String) : DocumentsEffect
}
