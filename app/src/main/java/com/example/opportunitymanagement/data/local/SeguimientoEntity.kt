package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Acción de seguimiento de una oportunidad (llamada, envío de propuesta, reunión...).
 * Los cambios de estado de la oportunidad también se guardan aquí, con el tipo
 * [TIPO_CAMBIO_ESTADO], para tener una sola línea de tiempo (historial).
 */
@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = OportunidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            // El historial pertenece a la oportunidad: si se elimina, se elimina con ella.
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ContactoEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactoId"],
            // Si se elimina el contacto, la acción queda en el historial sin contacto.
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("oportunidadId"), Index("usuarioId"), Index("contactoId")]
)
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    // Usuario responsable de la acción (el de la sesión al registrarla).
    val usuarioId: Long?,
    val tipoAccion: String,
    // Fecha y hora de la acción como "hora de reloj": medianoche UTC del día + minutos.
    val fechaHora: Long,
    val resultado: String,
    // Contacto de la organización con quien se hizo la acción (opcional).
    val contactoId: Long? = null,
    val proximaAccion: String = "",
    // Día del próximo seguimiento (medianoche UTC), o null si no hay próxima acción.
    val fechaProximoSeguimiento: Long? = null,
    val observaciones: String = ""
) {
    companion object {
        const val TIPO_CAMBIO_ESTADO = "Cambio de estado"
    }
}
