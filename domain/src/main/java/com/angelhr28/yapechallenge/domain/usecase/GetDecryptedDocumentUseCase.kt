package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

class GetDecryptedDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(document: Document): ByteArray {
        return repository.getDecryptedBytes(document)
    }
}
