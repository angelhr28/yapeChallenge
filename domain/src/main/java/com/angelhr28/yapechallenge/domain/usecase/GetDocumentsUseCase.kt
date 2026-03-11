package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener la lista de documentos, con filtro opcional por tipo.
 *
 * @param repository Repositorio de documentos.
 */
class GetDocumentsUseCase(private val repository: DocumentRepository) {
    /**
     * @param filter Tipo de documento para filtrar, o `null` para obtener todos.
     * @return Flujo reactivo con la lista de documentos.
     */
    operator fun invoke(filter: DocumentType? = null): Flow<List<Document>> {
        return if (filter != null) {
            repository.getDocumentsByType(filter)
        } else {
            repository.getAllDocuments()
        }
    }
}
