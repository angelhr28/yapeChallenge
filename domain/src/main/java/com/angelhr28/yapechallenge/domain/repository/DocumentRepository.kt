package com.angelhr28.yapechallenge.domain.repository

import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio para operaciones de documentos y registros de acceso.
 */
interface DocumentRepository {
    /** Obtiene todos los documentos como un flujo reactivo. */
    fun getAllDocuments(): Flow<List<Document>>

    /** Obtiene documentos filtrados por [type]. */
    fun getDocumentsByType(type: DocumentType): Flow<List<Document>>

    /** Obtiene un documento por su [id], o `null` si no existe. */
    suspend fun getDocumentById(id: Long): Document?

    /** Agrega un nuevo documento cifrando los bytes de origen. */
    suspend fun addDocument(name: String, mimeType: String, sourceBytes: ByteArray): Document

    /** Elimina el documento con el [id] especificado. */
    suspend fun deleteDocument(id: Long)

    /** Descifra y retorna los bytes del [document]. */
    suspend fun getDecryptedBytes(document: Document): ByteArray

    /** Obtiene los registros de acceso del documento con [documentId]. */
    fun getAccessLogs(documentId: Long): Flow<List<AccessLog>>

    /** Registra un acceso al documento con la acción y ubicación indicadas. */
    suspend fun logAccess(documentId: Long, action: com.angelhr28.yapechallenge.domain.model.AccessAction, location: String?)
}
