package com.example.opportunitymanagement.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.UsuarioUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(s: UsuarioUiState,
                  volver: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Mi perfil") },
            actions = { TextButton(onClick = volver) { Text("Volver") } }
        )
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (s.isLoading) {
                CircularProgressIndicator()
                return@Column
            }
            val rol = when(s.role){
                Role.VENDEDOR -> "Vendedor"
                Role.ADMINISTRADOR -> "Administrador"
            }
            PerfilCard(
                nombre = s.name,
                rol = rol,
                email = s.email,
                activo = s.isActive
            )
        }
    }
}

/** Ícono de perfil en la columna izquierda; todos los datos en la columna derecha. */
@Composable
fun PerfilCard(
    nombre: String,
    rol: String,
    email: String,
    activo: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nombre", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(nombre, style = MaterialTheme.typography.titleLarge)
                DatoPerfil("Correo", email)
                DatoPerfil("Estado de la cuenta", if (activo) "Activa" else "Inactiva")
                DatoPerfil("Tipo",rol)

            }
        }
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview(

) {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        ProfileScreen(
            UsuarioUiState(
                isLoading = false,
                name = "Ana Pérez",
                email = "ana.perez@ejemplo.cl",
                role = Role.VENDEDOR,
                isActive =  true
            ),
            volver = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCargandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        ProfileScreen(UsuarioUiState(isLoading = true), volver = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilCardPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        PerfilCard(
            nombre = "Ana Pérez",
            rol = "Vendedor",
            email = "ana.perez@ejemplo.cl",
            activo = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DatoPerfilPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { DatoPerfil("Correo", "ana.perez@ejemplo.cl") }
}