package com.angelhr28.yapechallenge.feature.documents

import com.angelhr28.yapechallenge.core.mvi.UiEffect
import com.angelhr28.yapechallenge.core.mvi.UiIntent
import com.angelhr28.yapechallenge.core.mvi.UiState
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType

data class DocumentsState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFilter: DocumentType? = null,
    val error: String? = null
) : UiState

sealed interface DocumentsIntent : UiIntent {
    data object LoadDocuments : DocumentsIntent
    data class FilterByType(val type: DocumentType?) : DocumentsIntent
    data class AddDocument(val name: String, val mimeType: String, val bytes: ByteArray) : DocumentsIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AddDocument) return false
            return name == other.name && mimeType == other.mimeType && bytes.contentEquals(other.bytes)
        }
        override fun hashCode(): Int = 31 * (31 * name.hashCode() + mimeType.hashCode()) + bytes.contentHashCode()
    }
    data class OpenDocument(val documentId: Long) : DocumentsIntent
}

sealed interface DocumentsEffect : UiEffect {
    data class NavigateToDetail(val documentId: Long) : DocumentsEffect
    data class ShowError(val message: String) : DocumentsEffect
    data class ShowSuccess(val message: String) : DocumentsEffect
}
