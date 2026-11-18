package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OrganizacionEntity

data class ContactoUiState(
    val id: Long = 0,
    val nombre: String = "",
    // null mientras no se elija una organización en el desplegable.
    val organizacionId: Long? = null,
    val cargo: String = "",
    val area: String = "",
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: String = "",
    val estado: String = "Por contactar",
    val fechaUltimoContacto: Long? = null,
    val observaciones: String = "",

    // Opciones del desplegable de organización.
    val organizaciones: List<OrganizacionEntity> = emptyList(),

    val nombreError: String? = null,
    val organizacionError: String? = null,
    val cargoError: String? = null,
    val areaError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null,
    val medioPreferenteError: String? = null,
    val estadoError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val cargaFallida: Boolean = false
)
