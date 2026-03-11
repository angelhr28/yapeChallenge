package com.angelhr28.yapechallenge.feature.detail.di

import com.angelhr28.yapechallenge.domain.usecase.DeleteDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetAccessLogsUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDecryptedDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentDetailUseCase
import com.angelhr28.yapechallenge.domain.usecase.LogAccessUseCase
import com.angelhr28.yapechallenge.feature.detail.DetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Modulo de inyeccion de dependencias Koin para la funcionalidad de detalle de documento.
 *
 * Provee los casos de uso y el ViewModel necesarios para la pantalla de detalle.
 */
val detailModule = module {
    factory { GetDocumentDetailUseCase(get()) }
    factory { GetDecryptedDocumentUseCase(get()) }
    factory { GetAccessLogsUseCase(get()) }
    factory { LogAccessUseCase(get()) }
    factory { DeleteDocumentUseCase(get()) }
    viewModel { DetailViewModel(get(), get(), get(), get(), get()) }
}
