package com.example.opportunitymanagement.viewmodel

data class OrganizacionUiState(
    val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val observaciones: String = "",

    val nombreError: String? = null,
    val tipoError: String? = null,
    val rubroError: String? = null,
    val comunaError: String? = null,
    val regionError: String? = null,
    val sitioWebError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val cargaFallida: Boolean = false
)
