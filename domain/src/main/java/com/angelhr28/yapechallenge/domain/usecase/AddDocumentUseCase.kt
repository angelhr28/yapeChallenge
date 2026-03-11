package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

class AddDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(name: String, mimeType: String, sourceBytes: ByteArray): Document {
        return repository.addDocument(name, mimeType, sourceBytes)
    }
}
