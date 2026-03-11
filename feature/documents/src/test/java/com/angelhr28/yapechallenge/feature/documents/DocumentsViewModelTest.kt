package com.angelhr28.yapechallenge.feature.documents

import app.cash.turbine.test
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.usecase.AddDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDocumentsUseCase: GetDocumentsUseCase
    private lateinit var addDocumentUseCase: AddDocumentUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getDocumentsUseCase = mockk()
        addDocumentUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads documents`() = runTest {
        val documents = listOf(
            Document(1L, "test.pdf", DocumentType.PDF, "/path", null, "application/pdf", 1024L)
        )
        every { getDocumentsUseCase(null) } returns flowOf(documents)

        val viewModel = DocumentsViewModel(getDocumentsUseCase, addDocumentUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.documents.size)
        assertEquals("test.pdf", state.documents[0].name)
    }

    @Test
    fun `filter by type updates state`() = runTest {
        every { getDocumentsUseCase(null) } returns flowOf(emptyList())
        val pdfDocs = listOf(
            Document(1L, "test.pdf", DocumentType.PDF, "/path", null, "application/pdf", 1024L)
        )
        every { getDocumentsUseCase(DocumentType.PDF) } returns flowOf(pdfDocs)

        val viewModel = DocumentsViewModel(getDocumentsUseCase, addDocumentUseCase)
        advanceUntilIdle()

        viewModel.processIntent(DocumentsIntent.FilterByType(DocumentType.PDF))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DocumentType.PDF, state.selectedFilter)
        assertEquals(1, state.documents.size)
    }

    @Test
    fun `open document emits navigation effect`() = runTest {
        every { getDocumentsUseCase(null) } returns flowOf(emptyList())

        val viewModel = DocumentsViewModel(getDocumentsUseCase, addDocumentUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(DocumentsIntent.OpenDocument(42L))
            val effect = awaitItem()
            assertEquals(DocumentsEffect.NavigateToDetail(42L), effect)
        }
    }

    @Test
    fun `add document emits success effect`() = runTest {
        every { getDocumentsUseCase(null) } returns flowOf(emptyList())
        val doc = Document(1L, "photo.jpg", DocumentType.IMAGE, "/path", null, "image/jpeg", 512L)
        coEvery { addDocumentUseCase(any(), any(), any()) } returns doc

        val viewModel = DocumentsViewModel(getDocumentsUseCase, addDocumentUseCase)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(
                DocumentsIntent.AddDocument("photo.jpg", "image/jpeg", ByteArray(512))
            )
            val effect = awaitItem()
            assertEquals(DocumentsEffect.ShowSuccess("Documento agregado exitosamente"), effect)
        }
    }
}
