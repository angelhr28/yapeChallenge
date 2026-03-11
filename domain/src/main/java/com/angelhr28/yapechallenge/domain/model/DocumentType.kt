package com.angelhr28.yapechallenge.domain.model

/**
 * Tipo de documento soportado por la aplicación.
 *
 * @property displayName Nombre legible del tipo de documento.
 * @property extensions Lista de extensiones de archivo asociadas.
 */
enum class DocumentType(val displayName: String, val extensions: List<String>) {
    PDF("PDF", listOf("pdf")),
    IMAGE("Imagen", listOf("jpg", "jpeg", "png", "webp", "bmp"));

    companion object {
        /**
         * Obtiene el tipo de documento a partir de la extensión del archivo.
         *
         * @param extension Extensión del archivo (sin punto).
         * @return El [DocumentType] correspondiente, o [IMAGE] por defecto.
         */
        fun fromExtension(extension: String): DocumentType {
            return entries.firstOrNull { type ->
                type.extensions.any { it.equals(extension, ignoreCase = true) }
            } ?: IMAGE
        }

        /**
         * Obtiene el tipo de documento a partir del tipo MIME.
         *
         * @param mimeType Tipo MIME del archivo.
         * @return El [DocumentType] correspondiente, o [IMAGE] por defecto.
         */
        fun fromMimeType(mimeType: String): DocumentType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType == "application/pdf" -> PDF
                else -> IMAGE
            }
        }
    }
}
