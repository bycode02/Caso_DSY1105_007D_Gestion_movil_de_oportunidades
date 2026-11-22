package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OrganizacionEntity

data class EventoUiState(
    val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    // null mientras no se elijan en el calendario y el reloj.
    val fechaInicio: Long? = null,
    val horaInicio: Int? = null,
    val horaTermino: Int? = null,
    val comuna: String = "",
    val region: String = "",
    // null mientras no se elija una organización.
    val organizacionId: Long? = null,
    val publicoEsperado: String = "",
    val condiciones: String = "",

    // Opciones del desplegable de organización.
    val organizaciones: List<OrganizacionEntity> = emptyList(),

    val nombreError: String? = null,
    val tipoError: String? = null,
    val fechaError: String? = null,
    val horaInicioError: String? = null,
    val horaTerminoError: String? = null,
    val comunaError: String? = null,
    val regionError: String? = null,
    val organizacionError: String? = null,
    val publicoEsperadoError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val cargaFallida: Boolean = false
)
