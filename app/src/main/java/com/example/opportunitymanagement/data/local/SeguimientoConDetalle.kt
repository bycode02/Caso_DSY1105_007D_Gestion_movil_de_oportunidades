package com.example.opportunitymanagement.data.local

import androidx.room.Embedded

/**
 * Acción del historial con el nombre del usuario que la registró y del contacto.
 * No es una tabla: Room la llena con el JOIN de SeguimientoDao.
 */
data class SeguimientoConDetalle(
    @Embedded val seguimiento: SeguimientoEntity,
    val usuarioNombre: String?,
    val contactoNombre: String?
)
