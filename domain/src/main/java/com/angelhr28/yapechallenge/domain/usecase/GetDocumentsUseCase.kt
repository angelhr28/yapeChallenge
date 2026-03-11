package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class GetDocumentsUseCase(private val repository: DocumentRepository) {
    operator fun invoke(filter: DocumentType? = null): Flow<List<Document>> {
        return if (filter != null) {
            repository.getDocumentsByType(filter)
        } else {
            repository.getAllDocuments()
        }
    }
}
