package com.example.opportunitymanagement.data.local

import androidx.room.Embedded

data class ContactoConOrganizacion(
    @Embedded val contacto: ContactoEntity,
    val organizacionNombre: String
)