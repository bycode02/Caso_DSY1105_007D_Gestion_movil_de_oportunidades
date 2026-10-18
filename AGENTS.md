# Reglas de arquitectura del proyecto

- Aplicación Android nativa con Kotlin, Jetpack Compose y MVVM.
- Los nombres de los paquetes deben estar en inglés y sus imports deben coincidir con la estructura de carpetas.
- Cada UiState debe ser una clase independiente en un archivo propio dentro del paquete `com.example.opportunitymanagement.viewmodel`.
- El archivo debe tener el mismo nombre que el UiState: por ejemplo, `OportunidadUiState.kt` o `ListaUiState.kt`.
- Nunca declarar un UiState dentro de una clase ViewModel ni en el mismo archivo que un ViewModel.
- No agrupar varios data class UiState en un mismo archivo.
- Los archivos UiState contienen únicamente su modelo de estado e imports. Las opciones y validaciones van en archivos separados.
- Cada componente visual Compose debe contar con su función @Preview, sin dependencias de cámara o base de datos en la previsualización.
- Guardar las capturas y evidencias en `Evidencia`, distinguiendo pruebas ejecutadas de comprobaciones pendientes.
