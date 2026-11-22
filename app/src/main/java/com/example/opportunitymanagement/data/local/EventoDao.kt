package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    // Todos los eventos con su organización, del más próximo al más lejano.
    @Query(
        """
        SELECT e.*, o.nombre AS organizacionNombre
        FROM eventos e
        INNER JOIN organizaciones o ON o.id = e.organizacionId
        ORDER BY e.fechaInicio ASC, e.horaInicio ASC, e.id ASC
        """
    )
    fun observarTodos(): Flow<List<EventoConOrganizacion>>

    // Eventos de una organización (sección "Eventos" del formulario de organización).
    @Query("SELECT * FROM eventos WHERE organizacionId = :organizacionId ORDER BY fechaInicio ASC, horaInicio ASC")
    fun observarPorOrganizacion(organizacionId: Long): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun buscar(id: Long): EventoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(evento: EventoEntity): Long

    @Update
    suspend fun actualizar(evento: EventoEntity): Int

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun eliminar(id: Long): Int
}
