package com.example.opportunitymanagement.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.RegisterUiState

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !state.isSaving && !state.registeredSuccessfully
    AuthPage(
        title = "Crea tu cuenta",
        description = "Comienza a organizar tus oportunidades y activaciones corporativas.",
        modifier = modifier
    ) {
        RegistrationRoleNotice()
        if (state.registeredSuccessfully) {
            AuthMessage(
                "Tu cuenta fue creada. Ya puedes iniciar sesión con tu correo y contraseña.",
                isError = false
            )
            AuthSubmitButton(
                "Ir a iniciar sesión",
                "Ingresando…",
                isLoading = false,
                onClick = onLogin
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "TUS DATOS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AuthTextField(
                        "Nombre completo",
                        state.name,
                        onNameChanged,
                        state.nameError,
                        enabled
                    )
                    AuthTextField(
                        "Correo electrónico", state.email, onEmailChanged, state.emailError,
                        enabled, KeyboardType.Email
                    )
                    AuthPasswordField(
                        label = "Contraseña",
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        error = state.passwordError,
                        enabled = enabled,
                        hint = "Al menos 8 caracteres",
                        imeAction = ImeAction.Next
                    )
                    AuthPasswordField(
                        label = "Confirmar contraseña",
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChanged,
                        error = state.confirmPasswordError,
                        enabled = enabled,
                        onDone = onRegister
                    )
                    state.errorMessage?.let { AuthMessage(it, isError = true) }
                    AuthSubmitButton(
                        text = "Crear cuenta",
                        loadingText = "Creando cuenta…",
                        isLoading = state.isSaving,
                        onClick = onRegister
                    )
                }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Ya tienes una cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onLogin, enabled = enabled) { Text("Iniciar sesión") }
            }
        }
    }
}

@Preview(name = "Registro", showBackground = true, widthDp = 390, heightDp = 960)
@Composable
private fun RegisterScreenPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        RegisterScreen(RegisterUiState(), {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Registro con errores", showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun RegisterErrorPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        RegisterScreen(
            RegisterUiState(
                nameError = "Ingresa tu nombre",
                emailError = "Ingresa un correo válido",
                passwordError = "Usa al menos 8 caracteres",
                confirmPasswordError = "Las contraseñas no coinciden"
            ), {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Cuenta creada", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterSuccessPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        RegisterScreen(RegisterUiState(registeredSuccessfully = true), {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Registro oscuro", showBackground = true, widthDp = 390, heightDp = 960)
@Composable
private fun RegisterDarkPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        RegisterScreen(RegisterUiState(), {}, {}, {}, {}, {}, {})
    }
}
