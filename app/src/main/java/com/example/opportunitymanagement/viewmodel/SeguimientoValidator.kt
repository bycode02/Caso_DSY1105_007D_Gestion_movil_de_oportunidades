package com.example.opportunitymanagement.viewmodel

class SeguimientoValidator {

    fun tipoAccionError(tipoAccion: String): String? = when {
        tipoAccion.isBlank() -> "Selecciona el tipo de acción"
        tipoAccion !in Opciones.tiposAccion -> "Selecciona un tipo válido"
        else -> null
    }

    fun fechaError(fecha: Long?): String? = when (fecha) {
        null -> "Selecciona la fecha de la acción"
        else -> null
    }

    fun horaError(hora: Int?): String? = when (hora) {
        null -> "Selecciona la hora de la acción"
        else -> null
    }

    fun resultadoError(resultado: String): String? = when {
        resultado.isBlank() -> "Describe el resultado de la acción"
        else -> null
    }

    /** Si hay fecha de próximo seguimiento, hay que indicar qué acción se hará. */
    fun proximaAccionError(proximaAccion: String, fechaProximo: Long?): String? = when {
        fechaProximo != null && proximaAccion.isBlank() -> "Indica la próxima acción"
        proximaAccion.isNotBlank() && proximaAccion !in Opciones.tiposAccion -> "Selecciona una acción válida"
        else -> null
    }

    /** Si hay próxima acción, hay que indicar cuándo; y no puede ser antes de esta acción. */
    fun fechaProximoError(proximaAccion: String, fechaProximo: Long?, fechaAccion: Long?): String? = when {
        proximaAccion.isNotBlank() && fechaProximo == null -> "Indica la fecha del próximo seguimiento"
        fechaProximo != null && fechaAccion != null && fechaProximo < fechaAccion ->
            "Debe ser igual o posterior a la fecha de la acción"
        else -> null
    }
}
