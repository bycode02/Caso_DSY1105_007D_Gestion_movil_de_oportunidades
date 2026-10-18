package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OportunidadEntity

data class ListaUiState(
    val items: List<OportunidadEntity> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)
