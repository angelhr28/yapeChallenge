package com.angelhr28.yapechallenge.data.repository

import com.angelhr28.yapechallenge.data.local.dao.AccessLogDao
import com.angelhr28.yapechallenge.data.local.dao.DocumentDao
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity
import com.angelhr28.yapechallenge.data.storage.EncryptedFileManager
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.model.DocumentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DocumentRepositoryImplTest {

    private lateinit var documentDao: DocumentDao
    private lateinit var accessLogDao: AccessLogDao
    private lateinit var encryptedFileManager: EncryptedFileManager
    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setup() {
        documentDao = mockk()
        accessLogDao = mockk()
        encryptedFileManager = mockk()
        repository = DocumentRepositoryImpl(documentDao, accessLogDao, encryptedFileManager)
    }

    @Test
    fun `getAllDocuments returns mapped domain models`() = runTest {
        val entities = listOf(
            DocumentEntity(1L, "doc1.pdf", "PDF", "/enc/doc1", null, "application/pdf", 1024L, 1000L, 1000L),
            DocumentEntity(2L, "img1.jpg", "IMAGE", "/enc/img1", null, "image/jpeg", 2048L, 2000L, 2000L)
        )
        every { documentDao.getAllDocuments() } returns flowOf(entities)

        val result = repository.getAllDocuments().first()

        assertEquals(2, result.size)
        assertEquals("doc1.pdf", result[0].name)
        assertEquals(DocumentType.PDF, result[0].type)
        assertEquals("img1.jpg", result[1].name)
        assertEquals(DocumentType.IMAGE, result[1].type)
    }

    @Test
    fun `getDocumentsByType filters correctly`() = runTest {
        val pdfEntities = listOf(
            DocumentEntity(1L, "doc1.pdf", "PDF", "/enc/doc1", null, "application/pdf", 1024L, 1000L, 1000L)
        )
        every { documentDao.getDocumentsByType("PDF") } returns flowOf(pdfEntities)

        val result = repository.getDocumentsByType(DocumentType.PDF).first()

        assertEquals(1, result.size)
        assertEquals(DocumentType.PDF, result[0].type)
    }

    @Test
    fun `getDocumentById returns null when not found`() = runTest {
        coEvery { documentDao.getDocumentById(999L) } returns null

        val result = repository.getDocumentById(999L)

        assertNull(result)
    }

    @Test
    fun `getDocumentById returns mapped document`() = runTest {
        val entity = DocumentEntity(1L, "doc.pdf", "PDF", "/enc/doc", null, "application/pdf", 1024L, 1000L, 1000L)
        coEvery { documentDao.getDocumentById(1L) } returns entity

        val result = repository.getDocumentById(1L)

        assertNotNull(result)
        assertEquals("doc.pdf", result?.name)
    }

    @Test
    fun `addDocument encrypts and stores document`() = runTest {
        val bytes = ByteArray(1024)
        every { encryptedFileManager.saveEncryptedFile(bytes, "pdf") } returns "/enc/new.pdf.enc"
        every { encryptedFileManager.getFileSize(bytes) } returns 1024L
        coEvery { documentDao.insertDocument(any()) } returns 1L

        val result = repository.addDocument("test.pdf", "application/pdf", bytes)

        assertEquals("test.pdf", result.name)
        assertEquals(DocumentType.PDF, result.type)
        assertEquals("/enc/new.pdf.enc", result.encryptedPath)
    }

    @Test
    fun `deleteDocument removes file and database entry`() = runTest {
        val entity = DocumentEntity(1L, "doc.pdf", "PDF", "/enc/doc.enc", null, "application/pdf", 1024L, 1000L, 1000L)
        coEvery { documentDao.getDocumentById(1L) } returns entity
        every { encryptedFileManager.deleteEncryptedFile("/enc/doc.enc") } just runs
        coEvery { documentDao.deleteDocument(1L) } just runs

        repository.deleteDocument(1L)

        coVerify { documentDao.deleteDocument(1L) }
    }

    @Test
    fun `logAccess inserts access log entity`() = runTest {
        val slot = slot<AccessLogEntity>()
        coEvery { accessLogDao.insertAccessLog(capture(slot)) } just runs

        repository.logAccess(1L, AccessAction.VIEW, "Lima")

        assertEquals(1L, slot.captured.documentId)
        assertEquals("VIEW", slot.captured.action)
        assertEquals("Lima", slot.captured.location)
    }

    @Test
    fun `getAccessLogs returns mapped logs`() = runTest {
        val entities = listOf(
            AccessLogEntity(1L, 1L, 1000L, "VIEW", "Lima"),
            AccessLogEntity(2L, 1L, 2000L, "DELETE", null)
        )
        every { accessLogDao.getAccessLogs(1L) } returns flowOf(entities)

        val result = repository.getAccessLogs(1L).first()

        assertEquals(2, result.size)
        assertEquals(AccessAction.VIEW, result[0].action)
        assertEquals("Lima", result[0].location)
        assertEquals(AccessAction.DELETE, result[1].action)
    }
}
