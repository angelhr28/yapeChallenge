package com.angelhr28.yapechallenge.domain.repository

import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentsByType(type: DocumentType): Flow<List<Document>>
    suspend fun getDocumentById(id: Long): Document?
    suspend fun addDocument(name: String, mimeType: String, sourceBytes: ByteArray): Document
    suspend fun deleteDocument(id: Long)
    suspend fun getDecryptedBytes(document: Document): ByteArray
    fun getAccessLogs(documentId: Long): Flow<List<AccessLog>>
    suspend fun logAccess(documentId: Long, action: com.angelhr28.yapechallenge.domain.model.AccessAction, location: String?)
}
