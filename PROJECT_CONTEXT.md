# Contexto Técnico del Proyecto — ToDoApp

## 1. Descripción General
**ToDoApp** es una aplicación nativa para Android desarrollada en **Kotlin** con **Jetpack Compose** y persistencia local estructurada sobre **Room Database (SQLite)**. Implementa el patrón arquitectónico **MVVM (Model-View-ViewModel)** con una capa de **Repository** e integración reactiva mediante **Kotlin Coroutines** y **StateFlow / Flow**.

No depende de servicios externos, servidores ni datos simulados: todas las operaciones CRUD se ejecutan y persisten directamente en el almacenamiento interno del dispositivo (`todo_app_database.db`).

---

## 2. Arquitectura del Sistema

El proyecto separa estrictamente las responsabilidades en cuatro capas desacopladas:

```text
+-------------------------------------------------------------------+
|                        CAPA UI (Jetpack Compose)                  |
|  MainActivity -> ToDoScreen -> TaskItemCard / EmptyStateView      |
|                             -> TaskFormDialog / DeleteDialog      |
+-------------------------------------------------------------------+
                                  |
                     Observa StateFlow<TaskUiState>
                     Envía eventos de usuario
                                  v
+-------------------------------------------------------------------+
|                     CAPA VIEWMODEL (Lógica MVVM)                  |
|  TaskViewModel + TaskUiState + TaskFilter + TaskViewModelFactory  |
+-------------------------------------------------------------------+
                                  |
                     Consume Flow<List<TaskEntity>>
                     Invoca funciones suspend
                                  v
+-------------------------------------------------------------------+
|                        CAPA REPOSITORY (Datos)                    |
|  TaskRepository (Interfaz) -> RoomTaskRepository (Implementación) |
+-------------------------------------------------------------------+
                                  |
                     Delega consultas SQL y transacciones
                                  v
+-------------------------------------------------------------------+
|                   CAPA PERSISTENCIA (Room / SQLite)               |
|  ToDoDatabase (Singleton) -> TaskDao -> TaskEntity (Tabla tasks)  |
+-------------------------------------------------------------------+
```

### Responsabilidad por Paquete
* **`com.angel.todoapp.data`**:
  * `TaskEntity.kt`: Define la entidad SQLite (`@Entity(tableName = "tasks")`) con `id` autogenerado, `title`, `description`, `isCompleted`, `createdAt` y `updatedAt`.
  * `TaskDao.kt`: Define las consultas SQL reactivas (`observeAllTasks(): Flow<List<TaskEntity>>`) y operaciones asíncronas (`insertTask`, `updateTask`, `updateCompletionStatus`, `deleteTask`).
  * `ToDoDatabase.kt`: Implementa `RoomDatabase` bajo el patrón Singleton seguro para hilos (`@Volatile` + `synchronized`).
  * `TaskRepository.kt`: Define la interfaz `TaskRepository` y su implementación `RoomTaskRepository`, aislando al ViewModel del acceso directo a la base de datos.
* **`com.angel.todoapp.viewmodel`**:
  * `TaskUiState.kt`: Representa el estado inmutable de la pantalla (`tasks`, `filteredTasks`, `filter`, contadores, estado de diálogos y errores de validación).
  * `TaskViewModel.kt`: Gestiona las operaciones CRUD dentro de `viewModelScope`, aplica la validación de título obligatorio y expone `StateFlow<TaskUiState>`.
* **`com.angel.todoapp.ui`**:
  * `theme/` (`Color.kt`, `Type.kt`, `Theme.kt`): Configura el sistema de diseño Material 3 con paleta en tonos verdes (`EmeraldPrimary #1B6B4A`, `SageSecondary #4C6357`) y neutros suaves (`NeutralBackground #F6F8F6`).
  * `components/`:
    * `TaskItemCard.kt`: Tarjeta individual de tarea con botón de estado completado, etiqueta de estado, marca de tiempo y botones de edición y eliminación.
    * `EmptyStateView.kt`: Pantalla Empty State personalizada cuando no existen registros en la base de datos o en el filtro activo.
    * `TaskFormDialog.kt`: Formulario modal para alta y edición de tareas con validación de título obligatorio.
    * `DeleteConfirmDialog.kt`: Diálogo de confirmación previa a la eliminación definitiva del registro.
  * `screens/ToDoScreen.kt`: Pantalla principal que ensambla la barra superior, el panel de métricas, los chips de filtrado, la lista reactiva (`LazyColumn`) y los diálogos modales.
* **Punto de entrada**:
  * `ToDoApplication.kt`: Inicializa la base de datos y el repositorio a nivel de aplicación.
  * `MainActivity.kt`: Configura `setContent` con `ToDoAppTheme` e inyecta el repositorio en `TaskViewModel`.

---

## 3. Estructura de Directorios

```text
ToDoApp/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/angel/todoapp/
│       │   │   ├── MainActivity.kt
│       │   │   ├── ToDoApplication.kt
│       │   │   ├── data/
│       │   │   │   ├── TaskDao.kt
│       │   │   │   ├── TaskEntity.kt
│       │   │   │   ├── TaskRepository.kt
│       │   │   │   └── ToDoDatabase.kt
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── DeleteConfirmDialog.kt
│       │   │   │   │   ├── EmptyStateView.kt
│       │   │   │   │   ├── TaskFormDialog.kt
│       │   │   │   │   └── TaskItemCard.kt
│       │   │   │   ├── screens/
│       │   │   │   │   └── ToDoScreen.kt
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt
│       │   │   │       ├── Theme.kt
│       │   │   │       └── Type.kt
│       │   │   └── viewmodel/
│       │   │       ├── TaskUiState.kt
│       │   │       └── TaskViewModel.kt
│       │   └── res/
│       │       └── values/
│       │           └── strings.xml
│       └── test/
│           └── java/com/angel/todoapp/
│               └── TaskViewModelTest.kt
├── Evidencias/
│   ├── 01_empty_state.png
│   ├── 02_validacion_titulo_vacio.png
│   ├── 03_listado_y_completado.png
│   ├── 04_edicion_tarea.png
│   ├── 05_confirmacion_eliminacion.png
│   └── 06_compilacion_y_tests.png
├── build.gradle.kts
├── gradle.properties
├── gradlew.bat
├── PROJECT_CONTEXT.md
├── README.md
├── TECHNICAL_GUIDE.md
└── settings.gradle.kts
```

---

## 4. Dependencias y Versiones

| Componente / Librería | Versión | Propósito |
| :--- | :--- | :--- |
| **Kotlin** | `2.0.21` | Lenguaje principal y plugin de compilación Compose |
| **Android Gradle Plugin (AGP)** | `8.7.2` | Sistema de construcción para Android |
| **KSP (Kotlin Symbol Processing)** | `2.0.21-1.0.27` | Procesador de anotaciones para generar código de Room |
| **Gradle** | `8.12` | Automatización de compilación y empaquetado |
| **Compile SDK / Target SDK** | `35` (Android 15) | Plataforma de compilación Android |
| **Min SDK** | `24` (Android 7.0) | Compatibilidad mínima de dispositivos |
| **Compose BOM** | `2024.10.01` | Gestión unificada de versiones de Jetpack Compose y Material 3 |
| **Room (`room-runtime`, `room-ktx`, `room-compiler`)** | `2.6.1` | Persistencia local sobre SQLite con soporte para Coroutines y Flow |
| **Lifecycle & ViewModel Compose** | `2.8.7` | Integración de `ViewModel` y `collectAsStateWithLifecycle` |
| **Kotlinx Coroutines Android / Test** | `1.9.0` | Ejecución asíncrona y pruebas unitarias de flujos reactivos |
| **JUnit** | `4.13.2` | Ejecución de pruebas unitarias de lógica y CRUD |

---

## 5. Estado del Proyecto y Cumplimiento de Requisitos

| Funcionalidad Requerida | Estado | Implementación Verificada |
| :--- | :--- | :--- |
| 1. Registrar tareas con título y descripción | Completado | `TaskFormDialog` -> `TaskViewModel.saveTask()` -> `TaskDao.insertTask()` |
| 2. Consultar tareas almacenadas | Completado | `TaskDao.observeAllTasks()` (`Flow`) -> `ToDoScreen` (`LazyColumn`) |
| 3. Editar tareas existentes | Completado | `TaskViewModel.openEditTaskDialog()` -> `TaskDao.updateTask()` |
| 4. Marcar y desmarcar como completadas | Completado | `TaskViewModel.toggleTaskCompletion()` -> `TaskDao.updateCompletionStatus()` |
| 5. Eliminar tareas con confirmación | Completado | `DeleteConfirmDialog` -> `TaskViewModel.confirmDeleteTask()` -> `TaskDao.deleteTask()` |
| 6. Mostrar pantalla Empty State | Completado | `EmptyStateView.kt` renderizado cuando la lista está vacía |
| 7. Conservar registros al cerrar y abrir | Completado | Base de datos persistente en disco `todo_app_database.db` con Room SQLite |
| 8. Validar que el título no esté vacío | Completado | Validación en `TaskViewModel.saveTask()` y estado de error en `OutlinedTextField` |
