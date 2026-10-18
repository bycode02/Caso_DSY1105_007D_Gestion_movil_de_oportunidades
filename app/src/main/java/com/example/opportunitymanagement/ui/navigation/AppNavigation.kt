package com.example.opportunitymanagement.ui.navigation

import com.example.opportunitymanagement.viewmodel.OportunidadViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.ui.opportunities.*
import com.example.opportunitymanagement.utils.QrScanner

@Composable
fun AppNavigation(repository: OportunidadRepository) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "acceso") {
        composable("acceso") {
            AccesoScreen {
                nav.navigate("menu") {
                    popUpTo("acceso") {
                        inclusive = true
                    }
                }
            }
        }
        composable("menu") {
            MenuScreen(
                { nav.navigate("lista") },
                { nav.navigate("qr") },
                { nav.navigate("acceso") { popUpTo("menu") { inclusive = true } } })
        }
        composable("lista") {
            val vm: OportunidadViewModel =
                viewModel(factory = viewModelFactory { initializer { OportunidadViewModel(repository) } })
            val lista by vm.lista.collectAsStateWithLifecycle()
            ListaOportunidadesScreen(
                lista,
                { nav.navigate("form/0") },
                { nav.navigate("form/$it") },
                { nav.popBackStack() })
        }
        composable("form/{id}") { entry ->
            val vm: OportunidadViewModel =
                viewModel(factory = viewModelFactory { initializer { OportunidadViewModel(repository) } })
            val s by vm.estado.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.cargar(
                    entry.arguments?.getString("id")?.toLongOrNull() ?: 0
                )
            }
            LaunchedEffect(s.guardadoExitosamente) { if (s.guardadoExitosamente) nav.popBackStack() }
            OportunidadFormScreen(s, vm::cambiar, vm::guardar, { nav.popBackStack() })
        }
        composable("qr") { QrScreen({ nav.navigate("form/$it") }, { nav.popBackStack() }) }
    }
}

@Composable
fun AccesoScreen(entrar: () -> Unit) {
    Column(Modifier
     .fillMaxSize()
     .padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text(
            "OPORTUNIDADES",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(16.dp))
        Text("Conecta con la próxima oportunidad", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        Text("Organiza eventos, prioriza oportunidades y prepara tu próxima activación corporativa.")
        Spacer(Modifier.height(32.dp))
        Card {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Acceso de demostración",
                    style = MaterialTheme.typography.titleMedium
                ); Text("Ejecutivo de prueba · Equipo comercial"); Text("Cuenta ficticia. Los registros se guardan en este dispositivo.")
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = entrar,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Iniciar sesión de prueba") }
    }
}

@Composable
fun MenuScreen(lista: () -> Unit, qr: () -> Unit, salir: () -> Unit) {
    Column(
        Modifier
         .fillMaxSize()
         .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Panel comercial", style = MaterialTheme.typography.headlineLarge)
        Text("Hola, Ejecutivo de prueba", style = MaterialTheme.typography.titleMedium)
        Text("Centraliza tus oportunidades y mantén al día cada gestión.")
        Card(
            onClick = lista,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Oportunidades",
                    style = MaterialTheme.typography.titleLarge
                ); Text("Registrar, consultar y actualizar →")
            }
        }
        OutlinedButton(
            onClick = qr,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Consultar oportunidad por QR") }
        Text("Primera entrega · Almacenamiento local", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = salir) { Text("Cerrar sesión de prueba") }
    }
}

fun idDesdeQr(texto: String): Long? =
    if (texto.startsWith("OPORTUNIDAD:")) texto.removePrefix("OPORTUNIDAD:").toLongOrNull()
        ?.takeIf { it > 0 } else null

@Composable
fun QrScreen(abrir: (Long) -> Unit, volver: () -> Unit) {
    val context = LocalContext.current
    var permiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var resultado by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permiso = it
    }
    Column(
        Modifier
         .fillMaxSize()
         .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = volver) { Text("Volver") }
        Text("Consultar por QR", style = MaterialTheme.typography.headlineMedium)
        Text("Escanea un código de prueba con el formato OPORTUNIDAD:1. El registro debe existir en este dispositivo.")
        if (!permiso) {
            Text("Se necesita acceso a la cámara. Si lo rechazaste permanentemente, habilítalo en Ajustes de Android.")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("Permitir cámara") }
        } else if (resultado == null) {
            QrScanner(
                onQrCodeScanned = { resultado = it },
                modifier = Modifier
                 .fillMaxWidth()
                 .height(320.dp),
                onError = { error = it })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } else {
            val id = idDesdeQr(resultado!!)
            Text(if (id == null) "Código no válido. Utiliza un QR de oportunidad." else "Oportunidad identificada: $id")
            if (id != null) Button(onClick = { abrir(id) }) { Text("Abrir oportunidad") }
            OutlinedButton(onClick = { resultado = null }) { Text("Escanear nuevamente") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccesoPreview() {
    MaterialTheme { AccesoScreen {} }
}

@Preview(showBackground = true)
@Composable
private fun MenuPreview() {
    MaterialTheme { MenuScreen({}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun QrPreview() {
    MaterialTheme { QrScreen({}, {}) }
}
