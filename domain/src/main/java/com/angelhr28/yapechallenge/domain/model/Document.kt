package com.angelhr28.yapechallenge.domain.model

data class Document(
    val id: Long = 0,
    val name: String,
    val type: DocumentType,
    val encryptedPath: String,
    val thumbnailPath: String? = null,
    val mimeType: String,
    val fileSize: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
