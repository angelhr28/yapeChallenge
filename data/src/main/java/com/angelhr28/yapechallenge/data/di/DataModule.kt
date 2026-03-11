package com.angelhr28.yapechallenge.data.di

import androidx.room.Room
import com.angelhr28.yapechallenge.core.security.CryptoManager
import com.angelhr28.yapechallenge.data.local.AppDatabase
import com.angelhr28.yapechallenge.data.repository.DocumentRepositoryImpl
import com.angelhr28.yapechallenge.data.storage.EncryptedFileManager
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Modulo de Koin que provee las dependencias de la capa de datos:
 * base de datos, DAOs, gestor de archivos cifrados y repositorio.
 */
val dataModule = module {
    single { CryptoManager() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "yape_challenge_database"
        ).build()
    }

    single { get<AppDatabase>().documentDao() }
    single { get<AppDatabase>().accessLogDao() }

    single { EncryptedFileManager(androidContext(), get()) }

    single<DocumentRepository> {
        DocumentRepositoryImpl(
            documentDao = get(),
            accessLogDao = get(),
            encryptedFileManager = get()
        )
    }
}
