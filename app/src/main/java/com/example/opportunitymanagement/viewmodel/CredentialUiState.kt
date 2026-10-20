package com.example.opportunitymanagement.viewmodel

data class CredentialUiState(
    val userId: Long = 0,
    val password: String = "",
    val confirmPassword: String = "",

    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)


