package com.example.opportunitymanagement.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme

@Composable
internal fun AuthPage(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "OPORTUNIDADES",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                content()
            }
        }
    }
}

@Composable
internal fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) ({ Text(error) }) else null,
        keyboardOptions = KeyboardOptions(
            capitalization = if (keyboardType == KeyboardType.Email)
                KeyboardCapitalization.None else KeyboardCapitalization.Words,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun AuthPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    enabled: Boolean = true,
    hint: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onDone: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val supporting = error ?: hint
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        isError = error != null,
        supportingText = if (supporting != null) ({ Text(supporting) }) else null,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            autoCorrectEnabled = false
        ),
        keyboardActions = KeyboardActions(onDone = {
            if (enabled) {
                focusManager.clearFocus()
                onDone()
            }
        }),
        trailingIcon = {
            TextButton(
                onClick = { visible = !visible },
                enabled = enabled,
                modifier = Modifier.semantics {
                    contentDescription = if (visible) "Ocultar $label" else "Mostrar $label"
                }
            ) {
                Text(
                    if (visible) "Ocultar" else "Mostrar",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun AuthSubmitButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Button(
        onClick = { focusManager.clearFocus(); onClick() },
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(if (isLoading) loadingText else text)
    }
}

@Composable
internal fun AuthMessage(message: String, isError: Boolean) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun RegistrationRoleNotice() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Cuenta de vendedor", style = MaterialTheme.typography.titleSmall)
            Text(
                "Registra y gestiona oportunidades con tu equipo comercial.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPagePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        AuthPage(
            "Bienvenido",
            "Tu próxima oportunidad comienza aquí."
        ) { Text("Formulario de acceso") }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthTextFieldPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        AuthTextField(
            "Correo electrónico",
            "correo",
            {},
            error = "Ingresa un correo válido",
            keyboardType = KeyboardType.Email
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPasswordFieldPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        AuthPasswordField("Contraseña", "Ejemplo123", {}, hint = "Al menos 8 caracteres")
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthSubmitButtonPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        AuthSubmitButton("Iniciar sesión", "Ingresando…", isLoading = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthMessagePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        AuthMessage("Revisa tus datos e inténtalo nuevamente.", isError = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationRoleNoticePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        RegistrationRoleNotice()
    }
}
