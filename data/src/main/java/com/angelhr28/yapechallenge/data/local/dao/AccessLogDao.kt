package com.angelhr28.yapechallenge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessLogDao {
    @Query("SELECT * FROM access_logs WHERE documentId = :documentId ORDER BY accessedAt DESC")
    fun getAccessLogs(documentId: Long): Flow<List<AccessLogEntity>>

    @Insert
    suspend fun insertAccessLog(accessLog: AccessLogEntity)
}
