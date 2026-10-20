package com.example.opportunitymanagement.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Query("SELECT * FROM oportunidades ORDER BY ultimaActualizacion DESC, id DESC")
    fun observarTodas(): Flow<List<OportunidadEntity>>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun buscar(id: Long): OportunidadEntity?
    @Insert
    suspend fun insertar(item: OportunidadEntity): Long
    @Update
    suspend fun actualizar(item: OportunidadEntity): Int
}
