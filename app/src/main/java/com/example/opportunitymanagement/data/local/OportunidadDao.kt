package com.example.opportunitymanagement.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    // LEFT JOIN para que también aparezcan las oportunidades sin responsable o sin evento.
    @Query(
        """
        SELECT o.*, u.nombre AS responsableNombre,
               e.nombre AS eventoNombre, org.nombre AS organizacionNombre,
               e.fechaInicio AS eventoFecha, e.comuna AS eventoComuna, e.region AS eventoRegion,
               (SELECT s.proximaAccion FROM seguimientos s
                WHERE s.oportunidadId = o.id AND s.tipoAccion <> 'Cambio de estado'
                ORDER BY s.fechaHora DESC, s.id DESC LIMIT 1) AS proximaAccion,
               (SELECT s.fechaProximoSeguimiento FROM seguimientos s
                WHERE s.oportunidadId = o.id AND s.tipoAccion <> 'Cambio de estado'
                ORDER BY s.fechaHora DESC, s.id DESC LIMIT 1) AS proximoSeguimiento
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

    // Guarda en Room lo que respondió el servidor (inserta o actualiza por id).
    // La copia completa la reemplaza SincronizacionDao, sin borrar el historial.
    @Upsert
    suspend fun upsert(item: OportunidadEntity)
}
