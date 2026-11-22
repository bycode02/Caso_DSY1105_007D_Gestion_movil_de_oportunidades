package com.example.opportunitymanagement.viewmodel

class OportunidadValidator {

    fun nombreError(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa el nombre de la oportunidad"
        else -> null
    }

    fun tipoError(tipo: String): String? = when {
        tipo.isBlank() -> "Selecciona un tipo"
        tipo !in Opciones.tipos -> "Selecciona un tipo válido"
        else -> null
    }

    fun descripcionError(descripcion: String): String? = when {
        descripcion.isBlank() -> "Ingresa una descripción"
        else -> null
    }

    fun fuenteError(fuente: String): String? = when {
        fuente.isBlank() -> "Indica la fuente de información"
        else -> null
    }

    fun prioridadError(prioridad: String): String? = when {
        prioridad.isBlank() -> "Selecciona una prioridad"
        prioridad !in Opciones.prioridades -> "Selecciona una prioridad válida"
        else -> null
    }

    fun potencialError(potencial: String): String? = when {
        potencial.isBlank() -> "Selecciona un potencial"
        potencial !in Opciones.potenciales -> "Selecciona un potencial válido"
        else -> null
    }

    fun estadoError(estado: String): String? = when {
        estado.isBlank() -> "Selecciona un estado"
        estado !in Opciones.estados -> "Selecciona un estado válido"
        else -> null
    }

    // Regla del equipo: toda oportunidad debe estar asociada a un evento.
    fun eventoError(eventoId: Long?): String? = when (eventoId) {
        null -> "Selecciona el evento de la oportunidad"
        else -> null
    }

    fun validar(state: OportunidadUiState): OportunidadUiState = state.copy(
        nombreError = nombreError(state.nombre),
        tipoError = tipoError(state.tipo),
        descripcionError = descripcionError(state.descripcion),
        fuenteError = fuenteError(state.fuente),
        prioridadError = prioridadError(state.prioridad),
        potencialError = potencialError(state.potencial),
        estadoError = estadoError(state.estado),
        eventoError = eventoError(state.eventoId)
    )

    fun tieneErrores(state: OportunidadUiState): Boolean = listOf(
        state.nombreError,
        state.tipoError,
        state.descripcionError,
        state.fuenteError,
        state.prioridadError,
        state.potencialError,
        state.estadoError,
        state.eventoError
    ).any { it != null }
}
