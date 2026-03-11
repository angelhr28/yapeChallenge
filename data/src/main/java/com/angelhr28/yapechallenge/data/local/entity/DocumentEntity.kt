package com.angelhr28.yapechallenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val encryptedPath: String,
    val thumbnailPath: String? = null,
    val mimeType: String,
    val fileSize: Long,
    val createdAt: Long,
    val updatedAt: Long
)
