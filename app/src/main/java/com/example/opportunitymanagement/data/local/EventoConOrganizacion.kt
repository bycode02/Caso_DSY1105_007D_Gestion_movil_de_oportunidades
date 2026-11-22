package com.example.opportunitymanagement.data.local

import androidx.room.Embedded

/**
 * Evento con el nombre de la organización que lo organiza.
 * No es una tabla: Room la llena con el JOIN de EventoDao.
 */
data class EventoConOrganizacion(
    @Embedded val evento: EventoEntity,
    val organizacionNombre: String
)
