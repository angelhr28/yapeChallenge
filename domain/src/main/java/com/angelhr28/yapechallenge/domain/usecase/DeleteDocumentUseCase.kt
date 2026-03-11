package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

/**
 * Caso de uso para eliminar un documento del repositorio.
 *
 * @param repository Repositorio de documentos.
 */
class DeleteDocumentUseCase(private val repository: DocumentRepository) {
    /**
     * @param documentId Identificador del documento a eliminar.
     */
    suspend operator fun invoke(documentId: Long) {
        repository.deleteDocument(documentId)
    }
}
