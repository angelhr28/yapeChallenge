package com.angelhr28.yapechallenge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import kotlinx.coroutines.flow.Flow

/** DAO para operaciones sobre la tabla de registros de acceso. */
@Dao
interface AccessLogDao {
    /** Obtiene los registros de acceso de un documento ordenados por fecha descendente. */
    @Query("SELECT * FROM access_logs WHERE documentId = :documentId ORDER BY accessedAt DESC")
    fun getAccessLogs(documentId: Long): Flow<List<AccessLogEntity>>

    /** Inserta un nuevo registro de acceso. */
    @Insert
    suspend fun insertAccessLog(accessLog: AccessLogEntity)
}
