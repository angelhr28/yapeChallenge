package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

/**
 * Caso de uso para agregar un nuevo documento cifrado al repositorio.
 *
 * @param repository Repositorio de documentos.
 */
class AddDocumentUseCase(private val repository: DocumentRepository) {
    /**
     * @param name Nombre del documento.
     * @param mimeType Tipo MIME del archivo.
     * @param sourceBytes Bytes sin cifrar del archivo original.
     * @return El [Document] creado y almacenado.
     */
    suspend operator fun invoke(name: String, mimeType: String, sourceBytes: ByteArray): Document {
        return repository.addDocument(name, mimeType, sourceBytes)
    }
}
