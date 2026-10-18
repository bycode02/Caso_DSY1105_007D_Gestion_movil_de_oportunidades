# Paquetes en inglés y ubicación del ViewModel

El ViewModel ya estaba implementado y conectado al formulario y la lista. Antes estaba
en `ui.oportunidad`. Ahora se encuentra en un paquete explícito `viewmodel`.

Paquete base: `com.example.opportunitymanagement`.

| Antes | Ahora |
|---|---|
| datos.local | data.local |
| datos.repository | data.repository |
| ui.oportunidad (pantallas/estado) | ui.opportunities |
| ui.oportunidad.OportunidadViewModel | viewmodel.OportunidadViewModel |
| ui.navegacion | ui.navigation |
| ui.theme.theme | ui.theme |

Ubicación del ViewModel en el proyecto:
`app/src/main/java/com/example/opportunitymanagement/viewmodel/OportunidadViewModel.kt`.

Responsabilidades: cargar registros, exponer estado con StateFlow, validar campos,
evitar envíos duplicados y guardar mediante OportunidadRepository. AppNavigation
crea el ViewModel con una fábrica y conecta `vm.lista`, `vm.estado`, `vm.cambiar`
y `vm.guardar` a las pantallas. Los previews siguen usando datos de ejemplo.

Se actualizaron declaraciones package, imports, directorios, namespace de Android,
paquetes de pruebas y ubicación del esquema exportado de Room.
El applicationId permanece igual para conservar la identidad instalada y los datos.
Las clases y los textos visibles mantienen sus nombres en español.

Las capturas del informe inicial son históricas, anteriores a esta refactorización.
El resultado de la nueva verificación está en `verificacion-packages.txt`.

Verificación completada: aplicación y APK de pruebas compilan; 11 pruebas unitarias aprobadas, 0 fallos y 0 errores.
