package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.ContactoConOrganizacion

data class ContactoListaUiState(
    val items: List<ContactoConOrganizacion> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null
)
