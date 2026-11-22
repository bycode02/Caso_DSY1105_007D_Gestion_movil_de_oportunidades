package com.example.opportunitymanagement.viewmodel

class EventoValidator {

    fun nombreError(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa el nombre del evento"
        else -> null
    }

    // Los tipos de evento son los mismos tipos de oportunidad del caso.
    fun tipoError(tipo: String): String? = when {
        tipo.isBlank() -> "Selecciona un tipo de evento"
        tipo !in Opciones.tipos -> "Selecciona un tipo válido"
        else -> null
    }

    fun fechaError(fechaInicio: Long?): String? = when (fechaInicio) {
        null -> "Selecciona la fecha del evento"
        else -> null
    }

    fun horaInicioError(horaInicio: Int?): String? = when (horaInicio) {
        null -> "Selecciona la hora de inicio"
        else -> null
    }

    fun horaTerminoError(horaInicio: Int?, horaTermino: Int?): String? = when {
        horaTermino == null -> "Selecciona la hora de término"
        horaInicio != null && horaTermino <= horaInicio -> "Debe ser posterior a la hora de inicio"
        else -> null
    }

    fun comunaError(comuna: String): String? = when {
        comuna.isBlank() -> "Indica la comuna"
        else -> null
    }

    fun regionError(region: String): String? = when {
        region.isBlank() -> "Indica la región"
        else -> null
    }

    fun organizacionError(organizacionId: Long?): String? = when {
        organizacionId == null || organizacionId <= 0L -> "Selecciona la organización responsable"
        else -> null
    }

    fun publicoEsperadoError(publicoEsperado: String): String? = when {
        publicoEsperado.isBlank() -> "Indica el público esperado"
        else -> null
    }
}
