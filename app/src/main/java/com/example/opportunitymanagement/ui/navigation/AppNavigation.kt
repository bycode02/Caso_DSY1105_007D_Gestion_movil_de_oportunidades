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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.ui.auth.LoginScreen
import com.example.opportunitymanagement.ui.auth.RegisterScreen
import com.example.opportunitymanagement.ui.opportunities.*
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.utils.QrScanner
import com.example.opportunitymanagement.viewmodel.AuthViewModelFactory
import com.example.opportunitymanagement.viewmodel.LoginViewModel
import com.example.opportunitymanagement.viewmodel.RegisterViewModel

@Composable
fun AppNavigation(repository: OportunidadRepository, authRepository: AuthRepository) {
    val nav = rememberNavController()
    val authFactory = remember(authRepository) { AuthViewModelFactory(authRepository) }
    // Sesión de la navegación: nombre del usuario autenticado, null si no hay sesión.
    var usuario by rememberSaveable { mutableStateOf<String?>(null) }
    NavHost(nav, startDestination = "login") {
        composable("login") {
            val vm: LoginViewModel = viewModel(factory = authFactory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.authenticatedUser?.id) {
                state.authenticatedUser?.let {
                    usuario = it.nombre
                    nav.navigate("menu") { popUpTo("login") { inclusive = true } }
                }
            }
            LoginScreen(
                state = state,
                onEmailChanged = vm::onEmailChanged,
                onPasswordChanged = vm::onPasswordChanged,
                onLogin = vm::login,
                onRegister = { nav.navigate("registro") }
            )
        }
        composable("registro") {
            val vm: RegisterViewModel = viewModel(factory = authFactory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            RegisterScreen(
                state = state,
                onNameChanged = vm::onNameChanged,
                onEmailChanged = vm::onEmailChanged,
                onPasswordChanged = vm::onPasswordChanged,
                onConfirmPasswordChanged = vm::onConfirmPasswordChanged,
                onRegister = vm::register,
                onLogin = {
                    if (!nav.popBackStack("login", inclusive = false)) {
                        nav.navigate("login") { popUpTo("registro") { inclusive = true } }
                    }
                }
            )
        }
        composable("menu") {
            MenuScreen(
                usuario.orEmpty(),
                { nav.navigate("lista") },
                { nav.navigate("qr") },
                {
                    usuario = null
                    nav.navigate("login") { popUpTo("menu") { inclusive = true } }
                })
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
fun MenuScreen(nombre: String, lista: () -> Unit, qr: () -> Unit, salir: () -> Unit) {
    Column(
        Modifier
         .fillMaxSize()
         .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Panel comercial", style = MaterialTheme.typography.headlineLarge)
        Text("Hola, $nombre", style = MaterialTheme.typography.titleMedium)
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
        Text("Almacenamiento local", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = salir) { Text("Cerrar sesión") }
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
private fun MenuPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { MenuScreen("Vendedor de prueba", {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun QrPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { QrScreen({}, {}) }
}
