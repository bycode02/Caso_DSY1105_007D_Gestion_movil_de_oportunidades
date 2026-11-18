package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface OrganizacionDao {

    @Query("SELECT * from organizaciones order by nombre asc")
     fun obtenerTodas(): Flow<List<OrganizacionEntity>>
    @Query("SELECT * from organizaciones where id= :id LIMIT 1")
    suspend fun encontrarPorId(id : Long): OrganizacionEntity
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(org : OrganizacionEntity): Long
    @Update
    suspend fun actualizar(org : OrganizacionEntity): Int
    @Query(value = "DELETE FROM organizaciones where id = :id")
    suspend fun eliminar(id : Long): Int

}