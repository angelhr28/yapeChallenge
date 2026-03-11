package com.angelhr28.yapechallenge.domain.model

/**
 * Registro de acceso a un documento.
 *
 * @property id Identificador único del registro.
 * @property documentId Identificador del documento accedido.
 * @property accessedAt Marca de tiempo del acceso.
 * @property action Tipo de acción realizada.
 * @property location Ubicación geográfica opcional del acceso.
 */
data class AccessLog(
    val id: Long = 0,
    val documentId: Long,
    val accessedAt: Long = System.currentTimeMillis(),
    val action: AccessAction,
    val location: String? = null
)

/**
 * Acción realizada sobre un documento.
 *
 * @property displayName Nombre legible de la acción.
 */
enum class AccessAction(val displayName: String) {
    VIEW("Visualización"),
    DELETE("Eliminación")
}
