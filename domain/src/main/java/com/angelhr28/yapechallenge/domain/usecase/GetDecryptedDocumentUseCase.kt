package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

/**
 * Caso de uso para obtener los bytes descifrados de un documento.
 *
 * @param repository Repositorio de documentos.
 */
class GetDecryptedDocumentUseCase(private val repository: DocumentRepository) {
    /**
     * @param document Documento cuyos bytes se desean descifrar.
     * @return Bytes descifrados del documento.
     */
    suspend operator fun invoke(document: Document): ByteArray {
        return repository.getDecryptedBytes(document)
    }
}
