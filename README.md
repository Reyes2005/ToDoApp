# ToDoApp — Gestión Local de Tareas en Android

Aplicación Android nativa desarrollada en **Kotlin** utilizando **Jetpack Compose**, **Room Database (SQLite)**, arquitectura **MVVM**, patrón **Repository**, **Coroutines** y **Flow**.

## Características Principales

1. **Registro de tareas**: Creación de actividades con título obligatorio y descripción opcional.
2. **Consulta reactiva**: Visualización en tiempo real mediante `Flow` y tarjetas Material 3, con resumen de métricas (`Total`, `Pendientes`, `Completadas`) y filtros por estado.
3. **Edición de tareas**: Modificación de título y descripción de cualquier registro existente.
4. **Control de avance**: Marcado y desmarcado de tareas como completadas con actualización inmediata en SQLite.
5. **Eliminación con confirmación**: Diálogo de seguridad antes de borrar permanentemente un registro.
6. **Pantalla Empty State**: Vista informativa dedicada cuando no existen tareas registradas.
7. **Persistencia local**: Almacenamiento permanente en el dispositivo mediante Room (`todo_app_database.db`).
8. **Validación de formulario**: Verificación de título no vacío con retroalimentación visual en español.

---

## Documentación del Proyecto (`docs/`)

Toda la documentación técnica y explicativa se encuentra organizada en la carpeta [`docs/`](./docs):

* **[`docs/REQUIREMENTS_AND_STACK.md`](./docs/REQUIREMENTS_AND_STACK.md)**: Guía técnica de requerimientos del entorno (JDK, Android SDK, Gradle, KSP), listado detallado de librerías y dependencias utilizadas, e instrucciones de configuración y compilación.
* **[`docs/TECHNICAL_GUIDE.md`](./docs/TECHNICAL_GUIDE.md)**: Manual paso a paso del *cómo y por qué* se construyó la aplicación, iniciando desde cero con la sintaxis de Kotlin, explicación de las tecnologías (Compose, Room, Coroutines, Flow), el flujo MVVM y el recorrido por cada archivo del proyecto.
* **[`docs/PROJECT_CONTEXT.md`](./docs/PROJECT_CONTEXT.md)**: Resumen arquitectónico por capas, árbol completo de directorios y matriz de cumplimiento de requisitos.

---

## Inicio Rápido desde Terminal

### 1. Compilar el APK Debug
```powershell
.\gradlew.bat assembleDebug
```
El APK instalable se genera en `app/build/outputs/apk/debug/app-debug.apk` y se encuentra disponible en la raíz del proyecto como [`ToDoApp.apk`](./ToDoApp.apk).

### 2. Ejecutar las Pruebas Unitarias (CRUD y MVVM)
```powershell
.\gradlew.bat testDebugUnitTest
```

### 3. Instalar en un Dispositivo o Emulador Conectado
```powershell
.\gradlew.bat installDebug
```

---

## Evidencias (`Evidencias/`)

En la carpeta [`Evidencias/`](./Evidencias) se incluyen las capturas de las funcionalidades implementadas y de la verificación de compilación y pruebas:
* [`01_empty_state.png`](./Evidencias/01_empty_state.png): Pantalla Empty State cuando no hay tareas registradas.
* [`02_validacion_titulo_vacio.png`](./Evidencias/02_validacion_titulo_vacio.png): Validación del campo título obligatorio en el formulario.
* [`03_listado_y_completado.png`](./Evidencias/03_listado_y_completado.png): Consulta de tarjetas de tareas y marcado de tarea completada.
* [`04_edicion_tarea.png`](./Evidencias/04_edicion_tarea.png): Diálogo de edición con datos precargados.
* [`05_confirmacion_eliminacion.png`](./Evidencias/05_confirmacion_eliminacion.png): Diálogo de confirmación previa a la eliminación.
* [`06_compilacion_y_tests.png`](./Evidencias/06_compilacion_y_tests.png): Evidencia de compilación con Gradle, generación de código Room KSP y ejecución de pruebas unitarias CRUD.
