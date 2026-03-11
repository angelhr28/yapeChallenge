package com.angelhr28.yapechallenge.domain.model

/**
 * Representa un documento almacenado de forma segura en la aplicación.
 *
 * @property id Identificador único del documento.
 * @property name Nombre del documento.
 * @property type Tipo de documento (PDF o imagen).
 * @property encryptedPath Ruta al archivo cifrado en disco.
 * @property thumbnailPath Ruta opcional a la miniatura del documento.
 * @property mimeType Tipo MIME del archivo original.
 * @property fileSize Tamaño del archivo en bytes.
 * @property createdAt Marca de tiempo de creación.
 * @property updatedAt Marca de tiempo de última actualización.
 */
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
