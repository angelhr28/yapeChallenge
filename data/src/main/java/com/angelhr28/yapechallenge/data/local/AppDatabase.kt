package com.angelhr28.yapechallenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.angelhr28.yapechallenge.data.local.dao.AccessLogDao
import com.angelhr28.yapechallenge.data.local.dao.DocumentDao
import com.angelhr28.yapechallenge.data.local.entity.AccessLogEntity
import com.angelhr28.yapechallenge.data.local.entity.DocumentEntity

/**
 * Base de datos principal de Room para la aplicacion.
 *
 * Contiene las tablas de documentos y registros de acceso.
 */
@Database(
    entities = [DocumentEntity::class, AccessLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /** Proporciona acceso al DAO de documentos. */
    abstract fun documentDao(): DocumentDao
    /** Proporciona acceso al DAO de registros de acceso. */
    abstract fun accessLogDao(): AccessLogDao
}
