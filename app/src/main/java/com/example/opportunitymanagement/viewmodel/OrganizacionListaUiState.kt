package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OrganizacionEntity

data class OrganizacionListaUiState(
    val items: List<OrganizacionEntity> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null
)
