package com.angelhr28.yapechallenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa un documento almacenado de forma segura.
 *
 * @property id Identificador unico autogenerado.
 * @property name Nombre del documento.
 * @property type Tipo de documento (PDF, IMAGE).
 * @property encryptedPath Ruta al archivo cifrado en almacenamiento interno.
 * @property thumbnailPath Ruta opcional a la miniatura del documento.
 * @property mimeType Tipo MIME del archivo original.
 * @property fileSize Tamano del archivo en bytes.
 * @property createdAt Marca de tiempo de creacion en milisegundos.
 * @property updatedAt Marca de tiempo de ultima actualizacion en milisegundos.
 */
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
