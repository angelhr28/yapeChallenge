package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener los registros de acceso de un documento.
 *
 * @param repository Repositorio de documentos.
 */
class GetAccessLogsUseCase(private val repository: DocumentRepository) {
    /**
     * @param documentId Identificador del documento.
     * @return Flujo reactivo con la lista de registros de acceso.
     */
    operator fun invoke(documentId: Long): Flow<List<AccessLog>> {
        return repository.getAccessLogs(documentId)
    }
}
