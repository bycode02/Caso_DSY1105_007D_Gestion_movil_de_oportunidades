package com.example.opportunitymanagement.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oportunidades")
data class OportunidadEntity(
 @PrimaryKey(autoGenerate = true) val id: Long = 0,
 val nombre: String, val tipo: String, val descripcion: String,
 val fuente: String, val prioridad: String, val potencial: String,
 val estado: String = "Evento identificado", val observaciones: String = "",
 val responsable: String = "Ejecutivo de prueba",
 val fechaDeteccion: Long = System.currentTimeMillis(),
 val ultimaActualizacion: Long = System.currentTimeMillis(),
 val referenciaGeografica: String = ""
)
