package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
@Dao
interface ContactoDao {

    @Query("SELECT * from contactos order by nombre asc")
     fun getAll(): Flow<List<ContactoEntity>>

    @Query("SELECT * from contactos where id= :id LIMIT 1")
    suspend fun findById(id: Long): ContactoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(contact: ContactoEntity): Long

    @Update
    suspend fun updteOrg(contact: ContactoEntity): Int

    @Query(value = "DELETE FROM contactos where id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * from contactos where organizacionId = :organizacionId order by nombre asc")
     fun observarPorOrganizacion(organizacionId: Long): Flow<List<ContactoEntity>>

    @Query(
        """
    SELECT c.*, o.nombre AS organizacionNombre
    FROM contactos c
    INNER JOIN organizaciones o ON o.id = c.organizacionId
    ORDER BY c.nombre
"""
    )
    fun observarTodos(): Flow<List<ContactoConOrganizacion>>

    // Guarda en Room lo que respondió el servidor (inserta o actualiza por id).
    @Upsert
    suspend fun upsert(item: ContactoEntity)
}
