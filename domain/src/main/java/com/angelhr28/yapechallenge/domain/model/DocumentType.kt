package com.angelhr28.yapechallenge.domain.model

enum class DocumentType(val displayName: String, val extensions: List<String>) {
    PDF("PDF", listOf("pdf")),
    IMAGE("Imagen", listOf("jpg", "jpeg", "png", "webp", "bmp"));

    companion object {
        fun fromExtension(extension: String): DocumentType {
            return entries.firstOrNull { type ->
                type.extensions.any { it.equals(extension, ignoreCase = true) }
            } ?: IMAGE
        }

        fun fromMimeType(mimeType: String): DocumentType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType == "application/pdf" -> PDF
                else -> IMAGE
            }
        }
    }
}
