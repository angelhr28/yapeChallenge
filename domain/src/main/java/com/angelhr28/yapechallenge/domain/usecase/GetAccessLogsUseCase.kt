package com.angelhr28.yapechallenge.domain.usecase

import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class GetAccessLogsUseCase(private val repository: DocumentRepository) {
    operator fun invoke(documentId: Long): Flow<List<AccessLog>> {
        return repository.getAccessLogs(documentId)
    }
}
