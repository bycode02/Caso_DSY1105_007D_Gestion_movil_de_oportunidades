package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.opportunitymanagement.data.repository.AuthRepository

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        LoginViewModel::class.java -> LoginViewModel(repository) as T
        RegisterViewModel::class.java -> RegisterViewModel(repository) as T
        else -> throw IllegalArgumentException("ViewModel no compatible: ${modelClass.name}")
    }
}
