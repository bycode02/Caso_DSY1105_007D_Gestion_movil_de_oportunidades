package com.example.opportunitymanagement.viewmodel

class ContactoValidator {

    fun nombreError(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa el nombre del contacto"
        else -> null
    }

    fun organizacionError(organizacionId: Long?): String? = when {
        organizacionId == null || organizacionId <= 0L -> "Selecciona una organización"
        else -> null
    }

    fun cargoError(cargo: String): String? = when {
        cargo.isBlank() -> "Indica el cargo o función"
        else -> null
    }

    fun areaError(area: String): String? = when {
        area.isBlank() -> "Selecciona un área"
        area !in Opciones.areasContacto -> "Selecciona un área válida"
        else -> null
    }

    // Opcional: solo se valida el formato si se escribió algo.
    fun correoError(correo: String): String? = when {
        correo.isBlank() -> null
        else -> AuthValidator.emailError(correo)
    }

    // Opcional: entre 8 y 12 dígitos; se permiten espacios y un "+" inicial.
    fun telefonoError(telefono: String): String? {
        if (telefono.isBlank()) return null
        val limpio = telefono.trim()
        val digitos = limpio.count { it.isDigit() }
        return when {
            !telefonoPattern.matches(limpio) -> "Usa solo números, espacios y un + inicial"
            digitos !in 8..12 -> "El teléfono debe tener entre 8 y 12 dígitos"
            else -> null
        }
    }

    fun medioPreferenteError(medio: String): String? = when {
        medio.isBlank() -> "Selecciona un medio de contacto"
        medio !in Opciones.mediosContacto -> "Selecciona un medio válido"
        else -> null
    }

    fun estadoError(estado: String): String? = when {
        estado.isBlank() -> "Selecciona un estado"
        estado !in Opciones.estadosContacto -> "Selecciona un estado válido"
        else -> null
    }

    private companion object {
        val telefonoPattern = Regex("^\\+?[0-9 ]+$")
    }
}
