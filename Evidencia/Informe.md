> Actualización: los paquetes ahora están en inglés. Las capturas siguientes corresponden a la versión anterior. Consulte Actualizacion-paquetes.md y verificacion-packages.txt.

# Informe de desarrollo — primera entrega DSY1105 / 007D

Proyecto: Gestión móvil de oportunidades y activaciones corporativas.

Implementación Android nativa: Kotlin, Jetpack Compose, Material 3, MVVM, Room y StateFlow.
Fecha: 18 de octubre de 2026. Desarrollo apoyado con IA; el equipo debe revisar y validar la entrega.

## Alcance y estado de la evidencia

Se implementó el módulo de oportunidades. El acceso es una demostración con identidad ficticia;
la autenticación completa y los demás módulos del caso se reservan para avances posteriores.

Las imágenes de este informe son capturas de un visor del código fuente real del proyecto.
**No son capturas de Android Studio ni de la aplicación ejecutándose.** Cada captura identifica
el archivo y las líneas mostradas. Se incluyen explicaciones y resultados para distinguir
implementación, compilación, pruebas unitarias y pruebas pendientes.

El emulador Pixel_7_API_34 no inició: Android Emulator 37.2.12 rechazó HAXM y solicitó WHPX.
También falló el intento sin aceleración. No había un teléfono Android conectado.
Por tanto, no se verificaron visualmente la app, los permisos, la lectura óptica de QR ni
la persistencia después de reiniciar el proceso. No se alteró la configuración de Windows.

La ubicación es una referencia ficticia opcional de Puente Alto, permitida por el caso.
No es una lectura del GPS. Si el docente exige la fila de ubicación del dispositivo
literalmente, ese recurso debe añadirse/probarse; la cámara es el recurso nativo implementado.

## Hitos del desarrollo

### Organización del proyecto

**Implementación:** Paquetes separados para datos locales, repositorio, pantallas, navegación y utilidades.

![Organización del proyecto](01_estructura.png)

**Resultado y verificación:** Se inspeccionaron los archivos y se compilaron las clases de la aplicación.

### Arquitectura MVVM

**Implementación:** Las pantallas reciben estado y callbacks. OportunidadViewModel procesa eventos, valida y utiliza OportunidadRepository; el repositorio utiliza el DAO.

![Arquitectura MVVM](02_mvvm.png)

**Resultado y verificación:** Las pruebas del ViewModel comprueban validación, guardado, edición, error y prevención de envíos duplicados.

### Navegación

**Implementación:** MainActivity construye el repositorio y abre AppNavigation. Rutas: acceso, menú, lista, formulario con id y lector QR.

![Navegación](03_navegacion.png)

**Resultado y verificación:** Las rutas y pantallas compilan. La ejecución de la secuencia completa en dispositivo sigue pendiente.

### Listado de oportunidades

**Implementación:** La lista presenta nombre, tipo, prioridad, potencial y estado; permite crear y abrir un registro para editar. Incluye estados vacío, carga y error.

![Listado de oportunidades](04_lista.png)

**Resultado y verificación:** Componente compilado y previews definidos. La prueba de interfaz está compilada, pendiente de ejecución.

### Registro y edición

**Implementación:** Formulario con nombre, tipo, descripción, fuente, prioridad, potencial, estado, observaciones y referencia geográfica ficticia opcional.

![Registro y edición](05_formulario.png)

**Resultado y verificación:** Las pruebas de lógica verifican que editar conserva el identificador y la fecha de detección. Falta verificación visual en Android.

### Validaciones

**Implementación:** Se rechazan campos obligatorios vacíos o compuestos por espacios y valores ajenos a las opciones permitidas. Los errores se guardan en UiState y se muestran junto a los campos.

![Validaciones](06_validaciones.png)

**Resultado y verificación:** Las pruebas unitarias verifican campos obligatorios, espacios y opciones inválidas. La evidencia visual de los mensajes en la app queda pendiente.

### Persistencia con Room

**Implementación:** Base de datos oportunidades.db, entidad con identificador autogenerado, inserción, consulta y actualización. Esquema exportado en app/schemas.

![Persistencia con Room](07_persistencia.png)

**Resultado y verificación:** Room generó su implementación y la aplicación compila. La prueba que cierra y reabre la base está compilada, pero no se ha ejecutado en dispositivo.

### UiState, StateFlow y Flow

**Implementación:** El DAO devuelve Flow; el ViewModel expone StateFlow; Compose observa con collectAsStateWithLifecycle. Guardado y errores forman parte del estado.

![UiState, StateFlow y Flow](08_reactividad.png)

**Resultado y verificación:** El cableado reactivo compila. La actualización visual de la lista después de guardar está cubierta por una prueba de interfaz aún pendiente de ejecución.

### Recurso nativo: cámara y QR

**Implementación:** CameraX y ML Kit leen códigos OPORTUNIDAD:<id>. Se solicita permiso de cámara, se controla el formato y se abre el registro local.

![Recurso nativo: cámara y QR](09_camara.png)

**Resultado y verificación:** La función de interpretación de QR pasó pruebas unitarias. Permisos, captura de cámara y reconocimiento óptico requieren prueba en Android.

### Pruebas del desarrollo

**Implementación:** Diez pruebas nuevas de lógica, además de las pruebas base del proyecto. Pruebas instrumentadas preparadas para flujo completo y persistencia real.

![Pruebas del desarrollo](10_pruebas.png)

**Resultado y verificación:** Consultar los XML y verificacion-build.txt de esta carpeta para resultados exactos. No se presenta una prueba instrumentada compilada como ejecutada.

### Previews por componente visual

**Implementación:** Vistas previas para acceso, menú, lista con datos y vacía, tarjeta, campos, formulario normal y con errores, cámara y tema.

![Previews por componente visual](11_previews.png)

**Resultado y verificación:** Las funciones Preview compilan. El lector usa LocalInspectionMode para evitar abrir la cámara en el editor. AppNavigation es el contenedor de integración; sus pantallas tienen previews independientes.

## Comprobaciones y pendientes para la entrega

- Consultar `verificacion-build.txt` para el resultado de compilación, pruebas y lint.
- Abrir los previews en Android Studio y revisar tamaños de texto, teclado y desplazamiento.
- Ejecutar `gradlew.bat connectedDebugAndroidTest` con un dispositivo de prueba disponible.
- La prueba `flujoCompleto` guarda capturas en la carpeta externa de la app: `files/Evidencia`.
- Copiar esas capturas a esta carpeta, preservando las evidencias de código.
- Crear una oportunidad, cerrarla completamente y volver a abrir la aplicación: comprobar que permanece.
- Modificarla y comprobar que se mantiene un único registro y se actualiza la lista.
- Probar QR válido, inválido, registro inexistente y permisos de cámara concedidos/rechazados.
- Revisar los datos y explicaciones del informe antes de presentarlo.
- Cada integrante debe realizar y registrar sus contribuciones. No se crearon commits atribuidos a terceros.

## Guion manual de demostración

1. Iniciar sesión de prueba y abrir Oportunidades.
2. Crear una oportunidad; intentar guardar vacía y revisar errores.
3. Completar los campos obligatorios y guardar.
4. Abrir el registro, modificar prioridad/estado y guardar.
5. Cerrar y reiniciar la aplicación; volver al listado.
6. Abrir el registro y anotar el texto OPORTUNIDAD:<id>.
7. Escanear un QR con ese texto desde el menú; consultar el registro.
8. Repetir con un QR inválido y con un identificador inexistente.

## Referencias de alcance

- Lineamientos 007D.pdf, primera entrega: oportunidades, MVVM, Room, validaciones, navegación y evidencias.
- Caso_DSY1105_007D_Gestión móvil de oportunidades y activaciones corporativas.pdf: tipos, estados, datos ficticios y alcance académico.

## Resultado final de la verificación

VERIFICACIÓN REAL DEL PROYECTO

ExampleUnitTest: 1 pruebas; 0 fallos; 0 errores
OportunidadViewModelTest: 5 pruebas; 0 fallos; 0 errores
ValidacionTest: 5 pruebas; 0 fallos; 0 errores

Compilación: BUILD SUCCESSFUL
APK de aplicación: generado
APK de pruebas instrumentadas: generado, no ejecutado
Lint: 0 errores, 26 advertencias

Ejecución Android: pendiente (emulador incompatible con HAXM).
No se generaron capturas de la aplicación ejecutándose.

![Verificación](12_verificacion.png)

## Avance del módulo de usuarios — 20 de octubre de 2026

Este avance prepara el modelo, la persistencia, el repositorio y la lógica de estado necesaria para registrar, consultar y administrar usuarios con roles `ADMINISTRADOR` y `VENDEDOR`. Las imágenes siguientes son capturas legibles del código fuente real; no corresponden a pantallas ejecutadas de la aplicación.

### Modelo de usuario y roles

Se creó `Role` como `enum class` y `UsuarioEntity` como entidad de Room. El usuario conserva identificador, nombre, correo, rol, estado activo y fechas de creación y actualización.

![Modelo de usuario y roles](13_user_model.png)

### DAO de usuarios

`UsuarioDao` contiene inserción, búsqueda por identificador y correo, observación del listado, actualización y activación o desactivación. Los retornos `Long`, entidad nullable e `Int` permiten comprobar el resultado de cada operación.

![DAO de usuarios](14_user_dao.png)

### Repositorio de usuarios

El repositorio centraliza las consultas, normaliza los correos, evita duplicados antes de insertar y transforma el número de filas actualizadas en un resultado booleano.

![Consultas y creación en el repositorio](15_user_repository_queries.png)

![Edición y activación en el repositorio](16_user_repository_updates.png)

### UsuarioUiState independiente

Siguiendo la regla de arquitectura, `UsuarioUiState` está en su propio archivo dentro de `viewmodel`. Contiene valores del formulario, errores por campo y estados de carga y guardado. Las contraseñas están en el estado del formulario y no en `UsuarioEntity`.

![UiState independiente](17_user_uistate.png)

### UsuarioViewModel

El ViewModel expone `StateFlow`, procesa cambios de nombre, correo, contraseña y confirmación, y limpia los errores asociados cuando cambia cada campo.

![Estado y eventos del ViewModel](18_user_viewmodel_state.png)

![Correo y confirmación](19_user_viewmodel_events.png)

El registro evita envíos duplicados, valida el formulario, consulta si el correo ya existe, crea la entidad y comunica éxito o error mediante el UiState.

![Inicio del registro](20_user_save_start.png)

![Control de duplicados y creación](21_user_duplicate_create.png)

![Resultado del registro](22_user_save_result.png)

Las validaciones cubren nombre obligatorio, longitud mínima, formato de correo, contraseña mínima y confirmación coincidente.

![Validación de nombre y correo](23_user_validation_identity.png)

![Validación de contraseña](24_user_validation_password.png)

![Resultado de validaciones](25_user_validation_result.png)

El ViewModel también permite cargar un usuario por identificador para una futura pantalla de consulta o edición y distingue un registro inexistente de un error inesperado.

![Carga de usuario](26_user_load_start.png)

![Resultado de carga y correo](27_user_load_result.png)

### Estado real de integración con Room

`AppDatabase` todavía contiene solamente `OportunidadEntity` y `OportunidadDao`. El módulo de usuarios no estará persistido hasta agregar `UsuarioEntity`, exponer `UsuarioDao` e implementar una migración de Room. Esta captura registra el estado actual sin presentarlo como completo.

![Estado actual de AppDatabase](28_database_status.png)

### Verificación realizada

Después de incorporar el módulo de usuarios se ejecutaron `assembleDebug` y `testDebugUnitTest`. La compilación terminó correctamente y se generó el APK. Las pruebas existentes pasan, pero aún no existen pruebas específicas del módulo de usuarios.

![Compilación del módulo de usuarios](29_build_status.png)

### Trabajo pendiente

- Registrar `UsuarioEntity` y `UsuarioDao` en `AppDatabase`.
- Crear la migración de Room antes de modificar la versión de la base de datos.
- Implementar credenciales sin almacenar contraseñas en texto plano.
- Separar `RegisterUiState`, `LoginUiState` y los ViewModels correspondientes.
- Crear las pantallas Compose de registro, inicio de sesión y administración, cada componente con `@Preview`.
- Añadir autorización por rol y pruebas unitarias e instrumentadas.
