package com.example.opportunitymanagement.viewmodel

data class OportunidadUiState(
    val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fuente: String = "",
    val prioridad: String = "",
    val potencial: String = "",
    val estado: String = "Evento identificado",
    val observaciones: String = "",
    val referenciaGeografica: String = "",
    val errores: Map<String, String> = emptyMap(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val cargaFallida: Boolean = false
)
