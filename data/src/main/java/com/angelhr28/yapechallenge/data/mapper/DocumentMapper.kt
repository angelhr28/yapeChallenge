package com.angelhr28.yapechallenge.data.mapper

import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.model.AccessLog
import com.angelhr28.yapechallenge.domain.model.Document
import com.angelhr28.yapechallenge.domain.model.DocumentType

fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        name = name,
        type = DocumentType.valueOf(type),
        encryptedPath = encryptedPath,
        thumbnailPath = thumbnailPath,
        mimeType = mimeType,
        fileSize = fileSize,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Document.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        name = name,
        type = type.name,
        encryptedPath = encryptedPath,
        thumbnailPath = thumbnailPath,
        mimeType = mimeType,
        fileSize = fileSize,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AccessLogEntity.toDomain(): AccessLog {
    return AccessLog(
        id = id,
        documentId = documentId,
        accessedAt = accessedAt,
        action = AccessAction.valueOf(action),
        location = location
    )
}

fun AccessLog.toEntity(): AccessLogEntity {
    return AccessLogEntity(
        id = id,
        documentId = documentId,
        accessedAt = accessedAt,
        action = action.name,
        location = location
    )
}
