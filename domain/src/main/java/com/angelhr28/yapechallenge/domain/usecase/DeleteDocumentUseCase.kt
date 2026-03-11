package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

class DeleteDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: Long) {
        repository.deleteDocument(documentId)
    }
}
