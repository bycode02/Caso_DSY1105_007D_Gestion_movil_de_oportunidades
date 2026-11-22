package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.ContactoEntity

data class SeguimientoUiState(
    // Oportunidad a la que pertenece la acción.
    val oportunidadId: Long = 0,
    val oportunidadNombre: String = "",
    val estadoOportunidad: String = "",

    val tipoAccion: String = "",
    // Día (medianoche UTC) y hora (minutos desde la medianoche) de la acción.
    val fecha: Long? = null,
    val hora: Int? = null,
    // Contacto opcional, elegido entre los de la organización del evento.
    val contactoId: Long? = null,
    val contactos: List<ContactoEntity> = emptyList(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximo: Long? = null,
    val observaciones: String = "",

    // Estado que se propone según el tipo de acción, y si el usuario acepta cambiarlo.
    val estadoSugerido: String? = null,
    val cambiarEstado: Boolean = false,

    val tipoAccionError: String? = null,
    val fechaError: String? = null,
    val horaError: String? = null,
    val resultadoError: String? = null,
    val proximaAccionError: String? = null,
    val fechaProximoError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val cargaFallida: Boolean = false
)
