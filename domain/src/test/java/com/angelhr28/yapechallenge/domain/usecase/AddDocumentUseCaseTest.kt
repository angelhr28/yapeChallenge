package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddDocumentUseCaseTest {

    private lateinit var repository: DocumentRepository
    private lateinit var useCase: AddDocumentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddDocumentUseCase(repository)
    }

    @Test
    fun `invoke adds document successfully`() = runTest {
        val expectedDocument = Document(
            id = 1L,
            name = "test.pdf",
            type = DocumentType.PDF,
            encryptedPath = "/encrypted/test.pdf.enc",
            mimeType = "application/pdf",
            fileSize = 2048L
        )
        val bytes = ByteArray(2048)

        coEvery { repository.addDocument("test.pdf", "application/pdf", bytes) } returns expectedDocument

        val result = useCase("test.pdf", "application/pdf", bytes)

        assertEquals(expectedDocument, result)
        coVerify { repository.addDocument("test.pdf", "application/pdf", bytes) }
    }

    @Test(expected = Exception::class)
    fun `invoke propagates exception from repository`() = runTest {
        val bytes = ByteArray(1024)
        coEvery { repository.addDocument(any(), any(), any()) } throws Exception("Storage full")

        useCase("test.pdf", "application/pdf", bytes)
    }
}
