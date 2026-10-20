package com.example.opportunitymanagement.viewmodel

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false,
    val registeredSuccessfully: Boolean = false,
    val errorMessage: String? = null
)
