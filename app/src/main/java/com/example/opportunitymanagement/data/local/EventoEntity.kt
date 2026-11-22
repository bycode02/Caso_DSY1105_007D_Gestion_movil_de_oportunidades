package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evento o actividad que organiza una organización (feria, jornada de beneficios, etc.).
 * Las oportunidades se asocian a un evento; así quedan unidas también a su organización.
 */
@Entity(
    tableName = "eventos",
    foreignKeys = [ForeignKey(
        entity = OrganizacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["organizacionId"],
        // No permite borrar una organización que todavía tiene eventos.
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("organizacionId"), Index("fechaInicio")]
)
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: String,
    val descripcion: String = "",
    // Día del evento: medianoche UTC en milisegundos (lo que entrega el DatePicker).
    val fechaInicio: Long,
    // Horario en minutos desde la medianoche (por ejemplo, 9:30 = 570).
    val horaInicio: Int,
    val horaTermino: Int,
    val comuna: String,
    val region: String,
    val organizacionId: Long,
    val publicoEsperado: String,
    val condiciones: String = ""
)
