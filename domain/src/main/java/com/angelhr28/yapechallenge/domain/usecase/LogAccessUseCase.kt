package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

/**
 * Caso de uso para registrar un acceso a un documento.
 *
 * @param repository Repositorio de documentos.
 */
class LogAccessUseCase(private val repository: DocumentRepository) {
    /**
     * @param documentId Identificador del documento accedido.
     * @param action Tipo de acción realizada sobre el documento.
     * @param location Ubicación geográfica opcional del acceso.
     */
    suspend operator fun invoke(documentId: Long, action: AccessAction, location: String? = null) {
        repository.logAccess(documentId, action, location)
    }
}
