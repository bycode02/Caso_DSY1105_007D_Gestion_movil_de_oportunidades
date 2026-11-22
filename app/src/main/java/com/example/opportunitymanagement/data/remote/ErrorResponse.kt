package com.example.opportunitymanagement.data.remote

/**
 * Cuerpo de los errores del backend.
 * errores: campo → mensaje, por ejemplo "eventoId" → "Selecciona el evento de la oportunidad".
 */
data class ErrorResponse(
    val mensaje: String? = null,
    val errores: Map<String, String>? = null
)
