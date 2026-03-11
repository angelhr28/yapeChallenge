package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetDocumentsUseCaseTest {

    private lateinit var repository: DocumentRepository
    private lateinit var useCase: GetDocumentsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetDocumentsUseCase(repository)
    }

    @Test
    fun `invoke without filter returns all documents`() = runTest {
        val documents = listOf(
            createDocument(1L, "doc1.pdf", DocumentType.PDF),
            createDocument(2L, "img1.jpg", DocumentType.IMAGE)
        )
        every { repository.getAllDocuments() } returns flowOf(documents)

        val result = useCase(null).first()

        assertEquals(2, result.size)
        verify { repository.getAllDocuments() }
    }

    @Test
    fun `invoke with PDF filter returns only PDFs`() = runTest {
        val pdfDocuments = listOf(
            createDocument(1L, "doc1.pdf", DocumentType.PDF)
        )
        every { repository.getDocumentsByType(DocumentType.PDF) } returns flowOf(pdfDocuments)

        val result = useCase(DocumentType.PDF).first()

        assertEquals(1, result.size)
        assertEquals(DocumentType.PDF, result[0].type)
        verify { repository.getDocumentsByType(DocumentType.PDF) }
    }

    @Test
    fun `invoke with IMAGE filter returns only images`() = runTest {
        val imageDocuments = listOf(
            createDocument(2L, "img1.jpg", DocumentType.IMAGE)
        )
        every { repository.getDocumentsByType(DocumentType.IMAGE) } returns flowOf(imageDocuments)

        val result = useCase(DocumentType.IMAGE).first()

        assertEquals(1, result.size)
        assertEquals(DocumentType.IMAGE, result[0].type)
        verify { repository.getDocumentsByType(DocumentType.IMAGE) }
    }

    @Test
    fun `invoke returns empty list when no documents exist`() = runTest {
        every { repository.getAllDocuments() } returns flowOf(emptyList())

        val result = useCase(null).first()

        assertEquals(0, result.size)
    }

    private fun createDocument(id: Long, name: String, type: DocumentType): Document {
        return Document(
            id = id,
            name = name,
            type = type,
            encryptedPath = "/path/$name.enc",
            mimeType = if (type == DocumentType.PDF) "application/pdf" else "image/jpeg",
            fileSize = 1024L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
