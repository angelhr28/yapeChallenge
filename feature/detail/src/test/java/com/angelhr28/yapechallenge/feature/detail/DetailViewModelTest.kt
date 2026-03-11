package com.angelhr28.yapechallenge.feature.detail

import app.cash.turbine.test
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.usecase.DeleteDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetAccessLogsUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDecryptedDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentDetailUseCase
import com.angelhr28.yapechallenge.domain.usecase.LogAccessUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getDocumentDetailUseCase: GetDocumentDetailUseCase
    private lateinit var getDecryptedDocumentUseCase: GetDecryptedDocumentUseCase
    private lateinit var getAccessLogsUseCase: GetAccessLogsUseCase
    private lateinit var logAccessUseCase: LogAccessUseCase
    private lateinit var deleteDocumentUseCase: DeleteDocumentUseCase
    private lateinit var viewModel: DetailViewModel

    private val testDocument = Document(
        id = 1L,
        name = "test.pdf",
        type = DocumentType.PDF,
        encryptedPath = "/path/test.pdf.enc",
        mimeType = "application/pdf",
        fileSize = 2048L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getDocumentDetailUseCase = mockk()
        getDecryptedDocumentUseCase = mockk()
        getAccessLogsUseCase = mockk()
        logAccessUseCase = mockk()
        deleteDocumentUseCase = mockk()

        viewModel = DetailViewModel(
            getDocumentDetailUseCase,
            getDecryptedDocumentUseCase,
            getAccessLogsUseCase,
            logAccessUseCase,
            deleteDocumentUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load document updates state and requests biometric auth`() = runTest {
        coEvery { getDocumentDetailUseCase(1L) } returns testDocument
        every { getAccessLogsUseCase(1L) } returns flowOf(emptyList())

        viewModel.effect.test {
            viewModel.processIntent(DetailIntent.LoadDocument(1L))
            advanceUntilIdle()

            val effect = awaitItem()
            assertEquals(DetailEffect.RequestBiometricAuth, effect)
        }

        val state = viewModel.state.value
        assertNotNull(state.document)
        assertEquals("test.pdf", state.document?.name)
    }

    @Test
    fun `on authenticated decrypts document and logs access`() = runTest {
        val decryptedBytes = ByteArray(100) { it.toByte() }
        coEvery { getDocumentDetailUseCase(1L) } returns testDocument
        every { getAccessLogsUseCase(1L) } returns flowOf(emptyList())
        coEvery { getDecryptedDocumentUseCase(testDocument) } returns decryptedBytes
        coEvery { logAccessUseCase(1L, AccessAction.VIEW, any()) } just runs

        viewModel.processIntent(DetailIntent.LoadDocument(1L))
        advanceUntilIdle()
        viewModel.processIntent(DetailIntent.OnAuthenticated)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isAuthenticated)
        assertNotNull(state.decryptedBytes)
        coVerify { logAccessUseCase(1L, AccessAction.VIEW, any()) }
    }

    @Test
    fun `load non-existent document shows error`() = runTest {
        coEvery { getDocumentDetailUseCase(999L) } returns null

        viewModel.effect.test {
            viewModel.processIntent(DetailIntent.LoadDocument(999L))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DetailEffect.ShowError)
        }
    }

    @Test
    fun `confirm delete removes document and navigates back`() = runTest {
        coEvery { getDocumentDetailUseCase(1L) } returns testDocument
        every { getAccessLogsUseCase(1L) } returns flowOf(emptyList())
        coEvery { logAccessUseCase(any(), any(), any()) } just runs
        coEvery { deleteDocumentUseCase(1L) } just runs
        coEvery { getDecryptedDocumentUseCase(testDocument) } returns ByteArray(10)

        viewModel.processIntent(DetailIntent.LoadDocument(1L))
        advanceUntilIdle()
        viewModel.processIntent(DetailIntent.OnAuthenticated)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(DetailIntent.ConfirmDelete)
            advanceUntilIdle()

            val effects = mutableListOf<DetailEffect>()
            effects.add(awaitItem())
            effects.add(awaitItem())

            assertTrue(effects.any { it is DetailEffect.ShowSuccess })
            assertTrue(effects.any { it is DetailEffect.NavigateBack })
        }

        coVerify { deleteDocumentUseCase(1L) }
    }

    @Test
    fun `request delete emits biometric auth request`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(DetailIntent.RequestDelete)
            val effect = awaitItem()
            assertEquals(DetailEffect.RequestDeleteBiometricAuth, effect)
        }
    }

    @Test
    fun `update location updates state`() = runTest {
        viewModel.processIntent(DetailIntent.UpdateLocation("Calle Lima 123"))
        advanceUntilIdle()

        assertEquals("Calle Lima 123", viewModel.state.value.currentLocation)
    }
}
