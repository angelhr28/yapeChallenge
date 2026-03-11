package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

/**
 * Caso de uso para obtener el detalle de un documento por su identificador.
 *
 * @param repository Repositorio de documentos.
 */
class GetDocumentDetailUseCase(private val repository: DocumentRepository) {
    /**
     * @param documentId Identificador del documento.
     * @return El [Document] encontrado, o `null` si no existe.
     */
    suspend operator fun invoke(documentId: Long): Document? {
        return repository.getDocumentById(documentId)
    }
}
