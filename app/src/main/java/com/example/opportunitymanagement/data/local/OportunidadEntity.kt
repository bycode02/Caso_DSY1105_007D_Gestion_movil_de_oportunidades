package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "oportunidades",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            // No permite borrar un usuario que todavía es responsable de oportunidades.
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = EventoEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            // No permite borrar un evento que todavía tiene oportunidades.
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("responsableId"), Index("eventoId")]
)
data class OportunidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String, val tipo: String, val descripcion: String,
    val fuente: String, val prioridad: String, val potencial: String,
    val estado: String = "Evento identificado", val observaciones: String = "",
    // Usuario responsable interno. Null solo en registros anteriores a la versión 4 de la base.
    val responsableId: Long? = null,
    val fechaDeteccion: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    val referenciaGeografica: String = "",
    // Evento al que se asocia la oportunidad (opcional, según el caso: "puede asociarse").
    val eventoId: Long? = null
)
