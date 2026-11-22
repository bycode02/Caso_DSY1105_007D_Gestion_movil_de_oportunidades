package com.example.opportunitymanagement.data.local

import androidx.room.Embedded

/**
 * Organización con su contacto principal (el primero registrado) y el total de contactos.
 * No es una tabla: Room la llena con la consulta de OrganizacionDao.observarConContacto().
 */
data class OrganizacionConContacto(
    @Embedded val organizacion: OrganizacionEntity,
    // Null cuando la organización todavía no tiene contactos.
    val contactoNombre: String?,
    val totalContactos: Int
)
