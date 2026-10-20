package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.EmailAlreadyRegisteredException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        if (_uiState.value.isSaving || _uiState.value.registeredSuccessfully) return
        _uiState.update { it.copy(name = name, nameError = null, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        if (_uiState.value.isSaving || _uiState.value.registeredSuccessfully) return
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        if (_uiState.value.isSaving || _uiState.value.registeredSuccessfully) return
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChanged(password: String) {
        if (_uiState.value.isSaving || _uiState.value.registeredSuccessfully) return
        _uiState.update {
            it.copy(
                confirmPassword = password,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.isSaving || state.registeredSuccessfully) return
        val nameError = if (state.name.isBlank()) "Ingresa tu nombre" else null
        val emailError = AuthValidator.emailError(state.email)
        val passwordError = AuthValidator.registrationPasswordError(state.password)
        val confirmError = when {
            state.confirmPassword.isBlank() -> "Confirma tu contraseña"
            state.password != state.confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        _uiState.update {
            it.copy(
                nameError = nameError, emailError = emailError,
                passwordError = passwordError, confirmPasswordError = confirmError,
                errorMessage = null
            )
        }
        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.register(state.name, state.email, state.password)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        registeredSuccessfully = true,
                        password = "",
                        confirmPassword = ""
                    )
                }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(isSaving = false, password = "", confirmPassword = "") }
                throw exception
            } catch (exception: EmailAlreadyRegisteredException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        emailError = "El correo ya está registrado"
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo crear la cuenta. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun reset() {
        if (!_uiState.value.isSaving) _uiState.value = RegisterUiState()
    }
}
