package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.CredentialEntity
import com.example.opportunitymanagement.data.repository.CredentialRepository
import com.example.opportunitymanagement.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class CredentialViewModel(
    private val repository: CredentialRepository,
    private val passwordHasher: PasswordHasher
): ViewModel() {

    private val _uiState = MutableStateFlow(CredentialUiState())
    val uiState : StateFlow<CredentialUiState> = _uiState.asStateFlow()

    fun onUserIdChanged(userId: Long) {
        _uiState.update {
            it.copy(
                userId = userId,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val passwordError = when {
            state.password.isBlank() ->
                "La contraseña es obligatoria"

            state.password.length < 8 ->
                "La contraseña debe tener al menos 8 caracteres"

            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() ->
                "Debes confirmar la contraseña"

            state.password != state.confirmPassword ->
                "Las contraseñas no coinciden"

            else -> null
        }

        _uiState.update {
            it.copy(
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        return passwordError == null &&
                confirmPasswordError == null
    }

    fun saveCredential() {
        val currentState = _uiState.value

        if (currentState.isSaving) {
            return
        }

        if (!validateForm()) {
            return
        }

        if (currentState.userId <= 0) {
            _uiState.update {
                it.copy(
                    errorMessage = "El usuario no es válido"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    savedSuccessfully = false,
                    errorMessage = null
                )
            }

            try {
                val hashResult = withContext(Dispatchers.Default) {
                    passwordHasher.createHash(
                        currentState.password
                    )
                }

                val credential = CredentialEntity(
                    id = 0,
                    userId = currentState.userId,
                    passwordHash = hashResult.hash,
                    passwordSalt = hashResult.salt
                )

                repository.createCredential(credential)

                _uiState.update {
                    it.copy(
                        password = "",
                        confirmPassword = "",
                        isSaving = false,
                        savedSuccessfully = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.message
                            ?: "El usuario ya tiene credenciales"
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudieron guardar las credenciales"
                    )
                }
            }
        }
    }
}