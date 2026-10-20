package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.UsuarioEntity
import com.example.opportunitymanagement.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = null,
                errorMessage = null
            )
        }
    }

    fun onPassChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun confirmPassword(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun saveUser() {
        val currentState = _uiState.value

        if (currentState.isSaving) {
            return
        }

        if (!validateForm()) {
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
                val emailExists = repository.findByEmail(
                    currentState.email
                ) != null

                if (emailExists) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            emailError = "El correo ya está registrado"
                        )
                    }
                    return@launch
                }

                val currentTime = System.currentTimeMillis()

                val user = UsuarioEntity(
                    id = currentState.id,
                    nombre = currentState.name.trim(),
                    email = currentState.email.trim().lowercase(),
                    role = currentState.role,
                    isActive = currentState.isActive,
                    createAt = currentTime,
                    updateAt = currentTime
                )

                repository.createUser(user)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo guardar el usuario"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val nameError = when {
            state.name.isBlank() ->
                "El nombre es obligatorio"

            state.name.trim().length < 3 ->
                "El nombre debe tener al menos 3 caracteres"

            else -> null
        }

        val emailError = when {
            state.email.isBlank() ->
                "El correo es obligatorio"

            !isValidEmail(state.email) ->
                "El formato del correo no es válido"

            else -> null
        }

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
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        return nameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val user = repository.findById(userId)

                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El usuario no existe"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        id = user.id,
                        name = user.nombre,
                        email = user.email,
                        role = user.role,
                        isActive = user.isActive,
                        isLoading = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar el usuario"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }

}