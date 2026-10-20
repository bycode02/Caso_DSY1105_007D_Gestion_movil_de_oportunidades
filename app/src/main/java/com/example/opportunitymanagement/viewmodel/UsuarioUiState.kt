package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.Role

data class UsuarioUiState(
    val id: Long = 0L,

    val name: String = "",
    val email: String = "",
    val role: Role = Role.VENDEDOR,
    val isActive: Boolean = true,

    val password: String = "",
    val confirmPassword: String = "",

    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)