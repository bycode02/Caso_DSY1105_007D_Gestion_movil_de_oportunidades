package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimientoDao {

    // Historial de una oportunidad, lo más reciente primero.
    @Query(
        """
        SELECT s.*, u.nombre AS usuarioNombre, c.nombre AS contactoNombre
        FROM seguimientos s
        LEFT JOIN usuario u ON u.id = s.usuarioId
        LEFT JOIN contactos c ON c.id = s.contactoId
        WHERE s.oportunidadId = :oportunidadId
        ORDER BY s.fechaHora DESC, s.id DESC
        """
    )
    fun observarPorOportunidad(oportunidadId: Long): Flow<List<SeguimientoConDetalle>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(seguimiento: SeguimientoEntity): Long

    // Guarda en Room lo que respondió el servidor (inserta o actualiza por id).
    @Upsert
    suspend fun upsert(item: SeguimientoEntity)
}
