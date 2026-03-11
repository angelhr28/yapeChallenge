package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository

class LogAccessUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: Long, action: AccessAction, location: String? = null) {
        repository.logAccess(documentId, action, location)
    }
}
