package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
@Dao
interface OrganizacionDao {

    @Query("SELECT * from organizaciones order by nombre asc")
     fun obtenerTodas(): Flow<List<OrganizacionEntity>>

    // Para la lista: cada organización con su primer contacto y cuántos tiene en total.
    // Room vuelve a emitir si cambia cualquiera de las dos tablas.
    @Query(
        """
        SELECT o.*,
            (SELECT c.nombre FROM contactos c WHERE c.organizacionId = o.id
             ORDER BY c.id LIMIT 1) AS contactoNombre,
            (SELECT COUNT(*) FROM contactos c WHERE c.organizacionId = o.id) AS totalContactos
        FROM organizaciones o
        ORDER BY o.nombre ASC
        """
    )
    fun observarConContacto(): Flow<List<OrganizacionConContacto>>
    @Query("SELECT * from organizaciones where id= :id LIMIT 1")
    suspend fun encontrarPorId(id : Long): OrganizacionEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(org : OrganizacionEntity): Long
    @Update
    suspend fun actualizar(org : OrganizacionEntity): Int
    @Query(value = "DELETE FROM organizaciones where id = :id")
    suspend fun eliminar(id : Long): Int

    // Guarda en Room lo que respondió el servidor (inserta o actualiza por id).
    @Upsert
    suspend fun upsert(item: OrganizacionEntity)
}
