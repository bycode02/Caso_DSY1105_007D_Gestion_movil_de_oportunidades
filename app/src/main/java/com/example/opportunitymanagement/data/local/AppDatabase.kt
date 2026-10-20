package com.example.opportunitymanagement.data.local

import android.content.Context
import androidx.room.*

@Database(entities = [OportunidadEntity::class,
                      UsuarioEntity::class],
    version = 2,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun oportunidades(): OportunidadDao
    abstract fun usuarios(): UsuarioDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null
        fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "oportunidades.db"
            ).build().also { instancia = it }
        }
    }
}
