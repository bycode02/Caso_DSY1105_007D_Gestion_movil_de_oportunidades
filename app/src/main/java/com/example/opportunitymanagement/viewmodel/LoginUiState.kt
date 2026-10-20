package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.UsuarioEntity

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: UsuarioEntity? = null
)
