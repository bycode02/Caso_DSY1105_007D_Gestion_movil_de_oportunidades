package com.example.opportunitymanagement.data.local
import android.content.Context
import androidx.room.*
@Database(entities = [OportunidadEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
 abstract fun oportunidades(): OportunidadDao
 companion object {
  @Volatile private var instancia: AppDatabase? = null
  fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
   instancia ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "oportunidades.db").build().also { instancia = it }
  }
 }
}
