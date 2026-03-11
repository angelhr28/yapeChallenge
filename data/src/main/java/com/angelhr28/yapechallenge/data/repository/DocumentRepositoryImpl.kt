package com.angelhr28.yapechallenge.data.repository

import com.angelhr28.yapechallenge.data.local.dao.AccessLogDao
import com.angelhr28.yapechallenge.data.local.dao.DocumentDao
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity
import com.angelhr28.yapechallenge.data.mapper.toDomain
import com.angelhr28.yapechallenge.data.storage.EncryptedFileManager
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementacion de [DocumentRepository] que coordina el almacenamiento
 * cifrado de archivos con la persistencia en base de datos local.
 *
 * @property documentDao DAO para operaciones sobre documentos.
 * @property accessLogDao DAO para registros de acceso.
 * @property encryptedFileManager Gestor de archivos cifrados.
 */
class DocumentRepositoryImpl(
    private val documentDao: DocumentDao,
    private val accessLogDao: AccessLogDao,
    private val encryptedFileManager: EncryptedFileManager
) : DocumentRepository {

    /** {@inheritDoc} */
    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** {@inheritDoc} */
    override fun getDocumentsByType(type: DocumentType): Flow<List<Document>> {
        return documentDao.getDocumentsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** {@inheritDoc} */
    override suspend fun getDocumentById(id: Long): Document? {
        return documentDao.getDocumentById(id)?.toDomain()
    }

    /**
     * Cifra el archivo, lo almacena y persiste los metadatos en la base de datos.
     * {@inheritDoc}
     */
    override suspend fun addDocument(
        name: String,
        mimeType: String,
        sourceBytes: ByteArray
    ): Document {
        val docType = DocumentType.fromMimeType(mimeType)
        val extension = when (docType) {
            DocumentType.PDF -> "pdf"
            DocumentType.IMAGE -> "img"
        }

        val encryptedPath = encryptedFileManager.saveEncryptedFile(sourceBytes, extension)
        val fileSize = encryptedFileManager.getFileSize(sourceBytes)
        val now = System.currentTimeMillis()

        val entity = DocumentEntity(
            name = name,
            type = docType.name,
            encryptedPath = encryptedPath,
            mimeType = mimeType,
            fileSize = fileSize,
            createdAt = now,
            updatedAt = now
        )

        val id = documentDao.insertDocument(entity)
        return entity.copy(id = id).toDomain()
    }

    /**
     * Elimina el archivo cifrado, su miniatura y el registro en base de datos.
     * {@inheritDoc}
     */
    override suspend fun deleteDocument(id: Long) {
        val document = documentDao.getDocumentById(id)
        if (document != null) {
            encryptedFileManager.deleteEncryptedFile(document.encryptedPath)
            document.thumbnailPath?.let { encryptedFileManager.deleteEncryptedFile(it) }
            documentDao.deleteDocument(id)
        }
    }

    /** {@inheritDoc} */
    override suspend fun getDecryptedBytes(document: Document): ByteArray {
        return encryptedFileManager.readDecryptedFile(document.encryptedPath)
    }

    /** {@inheritDoc} */
    override fun getAccessLogs(documentId: Long): Flow<List<AccessLog>> {
        return accessLogDao.getAccessLogs(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** {@inheritDoc} */
    override suspend fun logAccess(documentId: Long, action: AccessAction, location: String?) {
        val entity = AccessLogEntity(
            documentId = documentId,
            accessedAt = System.currentTimeMillis(),
            action = action.name,
            location = location
        )
        accessLogDao.insertAccessLog(entity)
    }
}
