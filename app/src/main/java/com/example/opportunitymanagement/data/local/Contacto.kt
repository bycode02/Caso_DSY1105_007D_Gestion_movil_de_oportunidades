package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contactos",
    foreignKeys = [ForeignKey(
        entity = OrganizacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["organizacionId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("organizacionId")]
)
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val organizacionId: Long,
    val cargo: String,
    val area: String,
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: String,
    val estado: String,
    val fechaUltimoContacto: Long? = null,
    val observaciones: String = ""
)