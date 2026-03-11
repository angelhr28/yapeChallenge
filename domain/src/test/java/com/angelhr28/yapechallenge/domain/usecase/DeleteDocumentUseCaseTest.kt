package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteDocumentUseCaseTest {

    private lateinit var repository: DocumentRepository
    private lateinit var useCase: DeleteDocumentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteDocumentUseCase(repository)
    }

    @Test
    fun `invoke deletes document by id`() = runTest {
        coEvery { repository.deleteDocument(1L) } just runs

        useCase(1L)

        coVerify { repository.deleteDocument(1L) }
    }
}
