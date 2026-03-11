package com.angelhr28.yapechallenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.angelhr28.yapechallenge.data.local.dao.AccessLogDao
import com.angelhr28.yapechallenge.data.local.dao.DocumentDao
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity

@Database(
    entities = [DocumentEntity::class, AccessLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun accessLogDao(): AccessLogDao
}
