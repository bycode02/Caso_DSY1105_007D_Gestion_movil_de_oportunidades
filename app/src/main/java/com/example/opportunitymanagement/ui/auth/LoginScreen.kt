package com.example.opportunitymanagement.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !state.isLoading && state.authenticatedUser == null
    AuthPage(
        title = "Inicia sesión",
        description = "Retoma tu gestión y conecta con la próxima oportunidad.",
        modifier = modifier
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "ACCEDE A TU CUENTA",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                AuthTextField(
                    label = "Correo electrónico",
                    value = state.email,
                    onValueChange = onEmailChanged,
                    error = state.emailError,
                    enabled = enabled,
                    keyboardType = KeyboardType.Email
                )
                AuthPasswordField(
                    label = "Contraseña",
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    error = state.passwordError,
                    enabled = enabled,
                    onDone = onLogin
                )
                state.errorMessage?.let { AuthMessage(it, isError = true) }
                state.authenticatedUser?.let {
                    AuthMessage(
                        "Sesión iniciada. Bienvenido, ${it.nombre}.",
                        isError = false
                    )
                }
                AuthSubmitButton(
                    text = "Iniciar sesión",
                    loadingText = "Ingresando…",
                    isLoading = state.isLoading,
                    enabled = enabled,
                    onClick = onLogin
                )
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¿Aún no tienes una cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRegister, enabled = enabled) { Text("Crear cuenta") }
        }
    }
}

@Preview(name = "Login", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        LoginScreen(LoginUiState(), {}, {}, {}, {})
    }
}

@Preview(name = "Login oscuro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginDarkPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        LoginScreen(LoginUiState(email = "vendedor@ejemplo.cl", isLoading = true), {}, {}, {}, {})
    }
}

@Preview(name = "Login con errores", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginErrorPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        LoginScreen(
            LoginUiState(
                email = "correo",
                emailError = "Ingresa un correo válido",
                passwordError = "Ingresa tu contraseña"
            ),
            {}, {}, {}, {}
        )
    }
}
