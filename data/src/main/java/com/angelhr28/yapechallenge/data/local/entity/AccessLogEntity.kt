package com.angelhr28.yapechallenge.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que registra los accesos realizados a un documento.
 *
 * Tiene una relacion de clave foranea con [DocumentEntity]; al eliminar
 * un documento se eliminan sus registros de acceso en cascada.
 *
 * @property id Identificador unico autogenerado.
 * @property documentId Identificador del documento asociado.
 * @property accessedAt Marca de tiempo del acceso en milisegundos.
 * @property action Accion realizada sobre el documento.
 * @property location Ubicacion opcional desde donde se accedio.
 */
@Entity(
    tableName = "access_logs",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class AccessLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val accessedAt: Long,
    val action: String,
    val location: String? = null
)
