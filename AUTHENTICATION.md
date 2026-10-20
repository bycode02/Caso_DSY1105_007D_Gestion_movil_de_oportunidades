# Registro, acceso y roles

Las pantallas están en el paquete `com.example.opportunitymanagement.ui.auth`.
Usan el tema existente, verde petróleo, Material 3, tarjetas y campos con borde.
Cada componente tiene una previsualización que no abre Room ni requiere un ViewModel.

## Dónde se define el rol

- `data/local/Role.kt` declara `ADMINISTRADOR` y `VENDEDOR`.
- `data/local/UsuarioEntity.kt` guarda el rol de cada usuario en `role`.
- `data/repository/AuthRepository.kt`, método `register()`, asigna
  `role = Role.VENDEDOR`. El método no recibe un rol desde la pantalla.
- `login()` comprueba la contraseña y que la cuenta esté activa, y devuelve el
  usuario con su rol guardado.

El registro público no permite crear administradores, tampoco convierte la primera
cuenta en administradora. La provisión inicial de un administrador y la pantalla
de gestión de usuarios quedan pendientes. Los cambios de rol deberán pasar por
una operación que compruebe los permisos del administrador que actúa.

El rol por sí solo no protege las operaciones: cuando se implemente la gestión de
usuarios y oportunidades, se deben comprobar los permisos en la lógica que las
ejecuta, además de mostrar u ocultar controles.

## Registro consistente

`AuthRepository.register()` genera el hash fuera del hilo principal y después
guarda usuario y credencial dentro de una transacción Room. Si cualquiera de las
inserciones falla, ambas se revierten. Rechaza correos duplicados entre registros
de este flujo y normaliza espacios exteriores y mayúsculas del correo.

Los DAO, entidades y `PasswordHasher` existentes se reutilizan.
`UsuarioViewModel` y `CredentialViewModel` siguen disponibles para sus flujos
individuales. Para el formulario completo se utiliza `RegisterViewModel`, que
coordina el registro mediante `AuthRepository`, sin encadenar dos guardados
independientes desde Compose.

## Conexión a la navegación

Las pantallas ya están conectadas: `MainActivity` crea `AuthRepository` y
`AppNavigation` comienza en la ruta `login`, con la ruta `registro` y el cierre
de sesión desde el menú. El acceso de demostración se eliminó. Los fragmentos
siguientes documentan cómo se hizo esa conexión.

Prepara una instancia compartida del repositorio:

```kotlin
val authRepository = AuthRepository(
    AppDatabase.obtener(applicationContext)
)
val authFactory = AuthViewModelFactory(authRepository)
```

Dentro del destino que tú definas para login:

```kotlin
val vm: LoginViewModel = viewModel(factory = authFactory)
val state by vm.uiState.collectAsStateWithLifecycle()

LoginScreen(
    state = state,
    onEmailChanged = vm::onEmailChanged,
    onPasswordChanged = vm::onPasswordChanged,
    onLogin = vm::login,
    onRegister = onOpenRegister
)
```

`onOpenRegister` es el callback que tú implementarás para abrir el registro.
Cuando `state.authenticatedUser` deje de ser `null`, puedes consumir el resultado
en un `LaunchedEffect(state.authenticatedUser?.id)` y realizar la navegación.
El rol está en `state.authenticatedUser?.role`.
Conserva la sesión en un propietario adecuado al flujo y retira el destino de
login de la pila al entrar. Al cerrar sesión limpia ese usuario; si reutilizas el
ViewModel de login, llama a `reset()`.

Dentro del destino de registro:

```kotlin
val vm: RegisterViewModel = viewModel(factory = authFactory)
val state by vm.uiState.collectAsStateWithLifecycle()

RegisterScreen(
    state = state,
    onNameChanged = vm::onNameChanged,
    onEmailChanged = vm::onEmailChanged,
    onPasswordChanged = vm::onPasswordChanged,
    onConfirmPasswordChanged = vm::onConfirmPasswordChanged,
    onRegister = vm::register,
    onLogin = onOpenLogin
)
```

`onOpenLogin` es tu callback de navegación. Al completar el registro se limpian
las contraseñas y se muestra el botón «Ir a iniciar sesión». No se inicia sesión
automáticamente. Cada destino debe tener su propio ViewModel; evita crear
instancias manuales en cada recomposición.

Imports para estos ejemplos:

```kotlin
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.ui.auth.LoginScreen
import com.example.opportunitymanagement.ui.auth.RegisterScreen
import com.example.opportunitymanagement.viewmodel.AuthViewModelFactory
import com.example.opportunitymanagement.viewmodel.LoginViewModel
import com.example.opportunitymanagement.viewmodel.RegisterViewModel
```

## Límites del alcance

La autenticación es local al dispositivo. La sesión persistente, recuperación de
contraseña, aprovisionamiento del administrador y autorización de las operaciones
del menú todavía no se implementaron.

La configuración existente declara `minSdk = 24`, pero el `PasswordHasher`
existente utiliza `PBKDF2WithHmacSHA256`, disponible desde API 26. Antes de admitir
Android 7 se debe resolver esa compatibilidad o decidir subir el mínimo a Android
8. Estas pantallas no cambian el mínimo de Android ni el formato de las credenciales.
Referencia: [SecretKeyFactory en Android](https://developer.android.com/reference/javax/crypto/SecretKeyFactory).
