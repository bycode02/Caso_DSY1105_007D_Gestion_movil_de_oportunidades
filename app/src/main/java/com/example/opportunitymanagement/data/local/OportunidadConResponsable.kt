package com.example.opportunitymanagement.data.local

import androidx.room.Embedded

/**
 * Resultado de la consulta que une una oportunidad con su responsable, su evento y la
 * organización de ese evento (Oportunidad → Evento → Organización).
 * No es una tabla: Room la llena a partir del JOIN en OportunidadDao.
 */
data class OportunidadConResponsable(
    @Embedded val oportunidad: OportunidadEntity,
    // Null cuando la oportunidad no tiene responsable asignado.
    val responsableNombre: String?,
    // Null cuando la oportunidad no está asociada a un evento.
    val eventoNombre: String? = null,
    val organizacionNombre: String? = null
)
