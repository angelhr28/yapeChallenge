package com.angelhr28.yapechallenge.domain.model

data class AccessLog(
    val id: Long = 0,
    val documentId: Long,
    val accessedAt: Long = System.currentTimeMillis(),
    val action: AccessAction,
    val location: String? = null
)

enum class AccessAction(val displayName: String) {
    VIEW("Visualización"),
    DELETE("Eliminación")
}
