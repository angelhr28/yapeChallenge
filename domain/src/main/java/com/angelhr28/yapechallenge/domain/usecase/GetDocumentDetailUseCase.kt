package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

class GetDocumentDetailUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: Long): Document? {
        return repository.getDocumentById(documentId)
    }
}
