package com.example.opportunitymanagement.viewmodel

internal object AuthValidator {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$")

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo electrónico"
        !emailPattern.matches(email.trim()) -> "Ingresa un correo válido"
        else -> null
    }

    fun registrationPasswordError(password: String): String? = when {
        password.isBlank() -> "Ingresa una contraseña"
        password.length < 8 -> "Usa al menos 8 caracteres"
        else -> null
    }
}
