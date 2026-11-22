package com.example.opportunitymanagement.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    // LEFT JOIN para que también aparezcan las oportunidades sin responsable o sin evento.
    @Query(
        """
        SELECT o.*, u.nombre AS responsableNombre,
               e.nombre AS eventoNombre, org.nombre AS organizacionNombre
        FROM oportunidades o
        LEFT JOIN usuario u ON u.id = o.responsableId
        LEFT JOIN eventos e ON e.id = o.eventoId
        LEFT JOIN organizaciones org ON org.id = e.organizacionId
        ORDER BY o.ultimaActualizacion DESC, o.id DESC
        """
    )
     fun observarTodas(): Flow<List<OportunidadConResponsable>>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun buscar(id: Long): OportunidadEntity?

    @Insert
    suspend fun insertar(item: OportunidadEntity): Long

    @Update
    suspend fun actualizar(item: OportunidadEntity): Int

    @Query(value = "DELETE FROM oportunidades where id = :id")
    suspend fun eliminar(id : Long): Int
}
