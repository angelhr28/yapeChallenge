package com.angelhr28.yapechallenge.di

import com.angelhr28.yapechallenge.core.security.BiometricHelper
import com.angelhr28.yapechallenge.core.security.CryptoManager
import com.angelhr28.yapechallenge.data.di.dataModule
import com.angelhr28.yapechallenge.feature.detail.di.detailModule
import com.angelhr28.yapechallenge.feature.documents.di.documentsModule
import org.koin.dsl.module

val appModule = module {
    single { BiometricHelper() }
    single { CryptoManager() }
}

val allModules = listOf(
    appModule,
    dataModule,
    documentsModule,
    detailModule
)
