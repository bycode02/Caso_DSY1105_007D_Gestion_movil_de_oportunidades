package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null,
                authenticatedUser = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null,
                authenticatedUser = null
            )
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading || state.authenticatedUser != null) return
        val emailError = AuthValidator.emailError(state.email)
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null
        _uiState.update {
            it.copy(emailError = emailError, passwordError = passwordError, errorMessage = null)
        }
        if (emailError != null || passwordError != null) return

        // Bloquea inmediatamente un segundo toque antes de lanzar la corrutina.
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val user = repository.login(state.email, state.password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        authenticatedUser = user,
                        errorMessage = if (user == null)
                            "No se pudo iniciar sesión. Revisa tus datos y que tu cuenta esté activa."
                        else null
                    )
                }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(isLoading = false, password = "") }
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        errorMessage = "No se pudo iniciar sesión. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun reset() {
        if (!_uiState.value.isLoading) _uiState.value = LoginUiState()
    }
}
