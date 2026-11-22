package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.ContactoEntity

data class OrganizacionUiState(
    val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val observaciones: String = "",

    // Contactos que pertenecen a esta organización (solo cuando ya está guardada).
    val contactos: List<ContactoEntity> = emptyList(),
    // true cuando ya se leyeron los contactos: evita avisar "sin contactos" mientras cargan.
    val contactosCargados: Boolean = false,

    val nombreError: String? = null,
    val tipoError: String? = null,
    val rubroError: String? = null,
    val comunaError: String? = null,
    val regionError: String? = null,
    val sitioWebError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    // Error para mostrar al usuario.
    val mensaje: String? = null,
    // Confirmación para mostrar al usuario (por ejemplo, al crear la organización).
    val aviso: String? = null,
    val cargaFallida: Boolean = false
)
