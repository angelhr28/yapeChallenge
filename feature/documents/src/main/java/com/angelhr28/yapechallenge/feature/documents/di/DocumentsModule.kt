package com.angelhr28.yapechallenge.feature.documents.di

import com.angelhr28.yapechallenge.domain.usecase.AddDocumentUseCase
import com.angelhr28.yapechallenge.domain.usecase.GetDocumentsUseCase
import com.angelhr28.yapechallenge.feature.documents.DocumentsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    factory { GetDocumentsUseCase(get()) }
    factory { AddDocumentUseCase(get()) }
    viewModel { DocumentsViewModel(get(), get()) }
}
