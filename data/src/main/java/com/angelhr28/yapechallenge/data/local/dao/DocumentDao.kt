package com.angelhr28.yapechallenge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

/** DAO para operaciones CRUD sobre la tabla de documentos. */
@Dao
interface DocumentDao {
    /** Obtiene todos los documentos ordenados por fecha de creacion descendente. */
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    /** Obtiene documentos filtrados por [type], ordenados por fecha de creacion descendente. */
    @Query("SELECT * FROM documents WHERE type = :type ORDER BY createdAt DESC")
    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>>

    /** Obtiene un documento por su [id], o `null` si no existe. */
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    /** Inserta o reemplaza un documento y retorna su identificador. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    /** Elimina el documento con el [id] indicado. */
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)
}
