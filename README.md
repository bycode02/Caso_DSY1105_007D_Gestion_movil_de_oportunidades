# Oportunidades corporativas — primera entrega

Aplicación Android nativa con Kotlin, Jetpack Compose, Material 3 y MVVM.

## Flujo
Acceso de demostración → Menú → Lista de oportunidades → Crear/editar.
Menú → Cámara QR → Consultar/editar una oportunidad local existente.

El acceso utiliza una identidad ficticia explícita. No es autenticación productiva.
Los tipos y estados corresponden al caso académico. Prioridad: Alta/Media/Baja;
potencial: Alto/Medio/Bajo. Estado inicial: Evento identificado.

## Arquitectura
- `data/local`: entidad Room, DAO reactivo y base de datos persistente.
- `data/repository`: acceso a los datos y control de actualización de registros existentes.
- `ui/opportunities`: componentes sin acceso directo a Room; ViewModel y UiState/StateFlow.
- `ui/navigation`: navegación Compose y pantallas de acceso, menú y cámara.
- `utils/QrScanner`: CameraX y ML Kit, permisos gestionados desde la pantalla.

Los formularios muestran errores de cada campo, impiden envíos repetidos, muestran
fallos de almacenamiento y vuelven a la lista tras guardar correctamente.
La lista observa el Flow de Room y refleja las modificaciones.
La edición conserva el identificador y fecha de detección, actualizando la fecha de modificación.

## Previews
Cada componente visual dispone de una función `@Preview` sin ViewModel ni base de datos.
El scanner muestra una referencia textual en modo de inspección, sin abrir la cámara.
`AppNavigation` conecta dependencias y rutas; se previsualizan sus pantallas individuales.
Abrir cada archivo de pantalla en Android Studio y seleccionar Split o Design.

## Compilación
JDK 21, Gradle 8.13, AGP 8.11.2, Kotlin 2.0.21, SDK 35, minSdk 24.
Se reemplazó la configuración inicial por una combinación compatible con el SDK instalado.
Room 2.6.1 usa kapt; los esquemas se exportan en `app/schemas`.

```text
gradlew.bat assembleDebug testDebugUnitTest
gradlew.bat connectedDebugAndroidTest
```

## Prueba de cámara
Crear primero una oportunidad. En su formulario aparece `OPORTUNIDAD:<id>`.
Generar un QR con ese texto y escanearlo desde el menú. Un texto ajeno o un
identificador inexistente muestra un mensaje; no abre enlaces ni crea registros automáticamente.
Probar permiso concedido, rechazado, código inválido y código válido.

## Ubicación
El caso permite una referencia ficticia y opcional, que se incorpora desde el formulario
y se guarda con Room. No se consulta el GPS real ni se registran recorridos.
La cámara es el recurso nativo elegido para esta entrega. La fila de ubicación del
lineamiento debe confirmarse con el docente si exige específicamente GPS del dispositivo.

## Evidencias
Ver `Evidencia/Informe.md`. Las pruebas instrumentadas guardan capturas reales en
`Android/data/com.example.caso_dsy1105_007d_gestion_movil_de_oportunidades/files/Evidencia`.
Estas capturas solo existen después de ejecutar las pruebas en un dispositivo compatible.
No confundir capturas de código con pruebas de ejecución.

## Alcance posterior
Autenticación completa, eventos, organizaciones/contactos, seguimiento, alertas,
filtros e historial se reservan para avances posteriores.
No se crearon commits en nombre de los integrantes. Cada integrante debe revisar
el trabajo y registrar sus propias contribuciones con su identidad real en Git.

## Paquetes en inglés

Paquete base: `com.example.opportunitymanagement`.

- `data.local`: entidad, DAO y AppDatabase.
- `data.repository`: OportunidadRepository.
- `viewmodel`: OportunidadViewModel, cada UiState en su propio archivo, opciones y validaciones separadas.
- `ui.opportunities`: pantallas y componentes.
- `ui.navigation`: AppNavigation.
- `ui.theme`: tema Compose.
- `utils`: utilidades nativas.

El ViewModel ya existía y se trasladó a un paquete explícito. La lista observa
`vm.lista`, el formulario observa `vm.estado` y envía acciones a `vm.cambiar`
y `vm.guardar`. El ViewModel usa el repositorio y este usa el DAO.

El applicationId se conserva para mantener la identidad de la aplicación instalada
y sus datos. No es el nombre de los paquetes del código. Las clases y los textos
de interfaz mantienen sus nombres en español; esta refactorización cambia paquetes e imports.
Las capturas del informe inicial documentan la versión anterior al cambio de paquetes.

## Regla de UiState

Cada UiState está en un archivo propio del paquete `viewmodel`. Ningún UiState se declara dentro del ViewModel ni en su archivo. Regla persistente registrada en `AGENTS.md`.
