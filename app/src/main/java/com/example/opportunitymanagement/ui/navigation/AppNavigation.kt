package com.example.opportunitymanagement.ui.navigation

import com.example.opportunitymanagement.viewmodel.ListaViewModel
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
import androidx.navigation.compose.*
import com.example.opportunitymanagement.ui.auth.LoginScreen
import com.example.opportunitymanagement.ui.auth.RegisterScreen
import com.example.opportunitymanagement.ui.opportunities.*
import com.example.opportunitymanagement.ui.organizaciones.OrganizacionFormScreen
import com.example.opportunitymanagement.ui.organizaciones.OrganizacionListaScreen
import com.example.opportunitymanagement.ui.profile.ProfileScreen
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.utils.QrScanner
import com.example.opportunitymanagement.viewmodel.LoginViewModel
import com.example.opportunitymanagement.viewmodel.OrganizacionListaViewModel
import com.example.opportunitymanagement.viewmodel.OrganizacioneViewModel
import com.example.opportunitymanagement.viewmodel.RegisterViewModel
import com.example.opportunitymanagement.viewmodel.SessionUiState
import com.example.opportunitymanagement.viewmodel.UsuarioViewModel

private val RUTAS_PUBLICAS = setOf("login", "registro")

/**
 * Grafo de navegación. La sesión llega desde [SessionUiState] (Room): al iniciar sesión se
 * pasa al menú y al cerrarla (o si la cuenta se desactiva) se vuelve al login.
 */
@Composable
fun AppNavigation(session: SessionUiState, onLogout: () -> Unit) {
    val nav = rememberNavController()
    // El destino inicial se decide una sola vez, con la sesión ya verificada.
    val inicio = remember { if (session.usuario != null) "menu" else "login" }
    val autenticado = session.usuario != null
    LaunchedEffect(autenticado) {
        val actual = nav.currentDestination?.route ?: return@LaunchedEffect
        val enRutaPublica = actual in RUTAS_PUBLICAS
        when {
            autenticado && enRutaPublica ->
                nav.navigate("menu") { popUpTo(nav.graph.id) { inclusive = true } }
            !autenticado && !enRutaPublica ->
                nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }
    NavHost(nav, startDestination = inicio) {
        composable("login") {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            val state by vm.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                onEmailChanged = vm::onEmailChanged,
                onPasswordChanged = vm::onPasswordChanged,
                onLogin = vm::login,
                onRegister = { nav.navigate("registro") }
            )
        }
        composable("registro") {
            val vm: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
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
                session.usuario?.nombre.orEmpty(),
                { nav.navigate("lista") },
                { nav.navigate("organizaciones") },
                { nav.navigate("qr") },
                {nav.navigate("profile")}
            )
        }
        composable("organizaciones") {
            val vm: OrganizacionListaViewModel = viewModel(factory = OrganizacionListaViewModel.Factory)
            val s by vm.uiState.collectAsStateWithLifecycle()
            OrganizacionListaScreen(
                s,
                agregar = { nav.navigate("organizacion/0") },
                editar = { nav.navigate("organizacion/$it") },
                eliminar = vm::eliminar,
                volver = { nav.popBackStack() }
            )
        }
        composable("profile") {
            val vm: UsuarioViewModel = viewModel(factory = UsuarioViewModel.Factory)
            val s by vm.uiState.collectAsStateWithLifecycle()
            ProfileScreen(s, volver = { nav.popBackStack() }, cerrarSesion = onLogout)
        }
        composable("lista") {
            val vm: ListaViewModel = viewModel(factory = ListaViewModel.Factory)
            val lista by vm.uiState.collectAsStateWithLifecycle()
            ListaOportunidadesScreen(
                lista,
                { nav.navigate("form/0") },
                { nav.navigate("form/$it") },
                vm::eliminar,
                { nav.popBackStack() })
        }
        composable("organizacion/{id}") { entry ->
            val vm: OrganizacioneViewModel = viewModel(factory = OrganizacioneViewModel.Factory)
            val s by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.cargar(entry.arguments?.getString("id")?.toLongOrNull() ?: 0)
            }
            LaunchedEffect(s.guardadoExitosamente) { if (s.guardadoExitosamente) nav.popBackStack() }
            OrganizacionFormScreen(
                state = s,
                onNombreChange = vm::onNombreChange,
                onTipoChange = vm::onTipoChange,
                onRubroChange = vm::onRubroChange,
                onComunaChange = vm::onComunaChange,
                onRegionChange = vm::onRegionChange,
                onSitioWebChange = vm::onSitioWebChange,
                onObservacionesChange = vm::onObservacionesChange,
                onGuardar = vm::guardar,
                onVolver = { nav.popBackStack() },

            )
        }
        composable("form/{id}") { entry ->
            val vm: OportunidadViewModel = viewModel(factory = OportunidadViewModel.Factory)
            val s by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.cargar(
                    entry.arguments?.getString("id")?.toLongOrNull() ?: 0
                )
            }
            LaunchedEffect(s.guardadoExitosamente) { if (s.guardadoExitosamente) nav.popBackStack() }
            OportunidadFormScreen(
                s = s,
                onNombreChange = vm::onNombreChange,
                onTipoChange = vm::onTipoChange,
                onDescripcionChange = vm::onDescripcionChange,
                onFuenteChange = vm::onFuenteChange,
                onPrioridadChange = vm::onPrioridadChange,
                onPotencialChange = vm::onPotencialChange,
                onEstadoChange = vm::onEstadoChange,
                onObservacionesChange = vm::onObservacionesChange,
                onReferenciaGeograficaChange = vm::onReferenciaGeograficaChange,
                guardar = vm::guardar,
                volver = { nav.popBackStack() }
            )
        }
        composable("qr") {
            QrScreen({ nav.navigate("form/$it") }, { nav.popBackStack() }) }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(nombre: String,
               lista: () -> Unit,
               organizaciones: () -> Unit,
               qr: () -> Unit,
               profile: () -> Unit) {
    TopAppBar(title = {Text("Menu")},
        actions = { TextButton(onClick = profile ) {Text("Perfil") }})
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
        Card(
            onClick = organizaciones,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Organizaciones",
                    style = MaterialTheme.typography.titleLarge
                ); Text("Quién organiza los eventos y actividades →")
            }
        }
        OutlinedButton(
            onClick = qr,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Consultar oportunidad por QR") }
        Text("Almacenamiento local", style = MaterialTheme.typography.bodySmall)

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
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { MenuScreen("Vendedor de prueba", {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun QrPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { QrScreen({}, {}) }
}
