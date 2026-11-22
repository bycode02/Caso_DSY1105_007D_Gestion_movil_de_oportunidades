package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OrganizacionConContacto

data class OrganizacionListaUiState(
    val items: List<OrganizacionConContacto> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null
)
