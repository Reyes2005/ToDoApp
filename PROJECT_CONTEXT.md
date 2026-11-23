# Contexto Tecnico del Proyecto — ToDoApp

## 1. Descripcion General
**ToDoApp** es una aplicacion nativa para Android desarrollada en **Kotlin** con **Jetpack Compose** y persistencia local estructurada sobre **Room Database (SQLite)**. Implementa el patron arquitectonico **MVVM (Model-View-ViewModel)** con una capa de **Repository** e integracion reactiva mediante **Kotlin Coroutines** y **StateFlow / Flow**.

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
                     Envia eventos de usuario
                                  v
+-------------------------------------------------------------------+
|                     CAPA VIEWMODEL (Logica MVVM)                  |
|  TaskViewModel + TaskUiState + TaskFilter + TaskViewModelFactory  |
+-------------------------------------------------------------------+
                                  |
                     Consume Flow<List<TaskEntity>>
                     Invoca funciones suspend
                                  v
+-------------------------------------------------------------------+
|                        CAPA REPOSITORY (Datos)                    |
|  TaskRepository (Interfaz) -> RoomTaskRepository (Implementacion) |
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
  * `TaskDao.kt`: Define las consultas SQL reactivas (`observeAllTasks(): Flow<List<TaskEntity>>`) y operaciones asincronas (`insertTask`, `updateTask`, `updateCompletionStatus`, `deleteTask`).
  * `ToDoDatabase.kt`: Implementa `RoomDatabase` bajo el patron Singleton thread-safe (`@Volatile` + `synchronized`).
  * `TaskRepository.kt`: Define la interfaz `TaskRepository` y su implementacion `RoomTaskRepository` que aisla al ViewModel del acceso directo a la base de datos.
* **`com.angel.todoapp.viewmodel`**:
  * `TaskUiState.kt`: Representa el estado inmutable de la pantalla (`tasks`, `filteredTasks`, `filter`, contadores, estado de dialogos y errores de validacion).
  * `TaskViewModel.kt`: Gestiona las operaciones CRUD dentro de `viewModelScope`, aplica la validacion de titulo obligatorio y expone `StateFlow<TaskUiState>`.
* **`com.angel.todoapp.ui`**:
  * `theme/` (`Color.kt`, `Type.kt`, `Theme.kt`): Configura el sistema de diseno Material 3 con paleta en tonos verdes (`EmeraldPrimary #1B6B4A`, `SageSecondary #4C6357`) y neutros suaves (`NeutralBackground #F6F8F6`).
  * `components/`:
    * `TaskItemCard.kt`: Tarjeta individual de tarea con boton de estado completado, etiqueta de estado, marca de tiempo y botones de edicion y eliminacion.
    * `EmptyStateView.kt`: Pantalla Empty State personalizada cuando no existen registros en la base de datos o en el filtro activo.
    * `TaskFormDialog.kt`: Formulario modal para alta y edicion de tareas con validacion de titulo obligatorio.
    * `DeleteConfirmDialog.kt`: Dialogo de confirmacion previa a la eliminacion definitiva del registro.
  * `screens/ToDoScreen.kt`: Pantalla principal que ensambla la barra superior, el panel de metricas, los chips de filtrado, la lista reactiva (`LazyColumn`) y los dialogos modales.
* **Punto de entrada**:
  * `ToDoApplication.kt`: Inicializa la base de datos y el repositorio a nivel de aplicacion.
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
└── settings.gradle.kts
```

---

## 4. Dependencias y Versiones

| Componente / Libreria | Version | Proposito |
| :--- | :--- | :--- |
| **Kotlin** | `2.0.21` | Lenguaje principal y plugin de compilacion Compose |
| **Android Gradle Plugin (AGP)** | `8.7.2` | Sistema de construccion para Android |
| **KSP (Kotlin Symbol Processing)** | `2.0.21-1.0.27` | Procesador de anotaciones para generar codigo de Room |
| **Gradle** | `8.12` | Automatizacion de compilacion y empaquetado |
| **Compile SDK / Target SDK** | `35` (Android 15) | Plataforma de compilacion Android |
| **Min SDK** | `24` (Android 7.0) | Compatibilidad minima de dispositivos |
| **Compose BOM** | `2024.10.01` | Gestion unificada de versiones de Jetpack Compose y Material 3 |
| **Room (`room-runtime`, `room-ktx`, `room-compiler`)** | `2.6.1` | Persistencia local sobre SQLite con soporte para Coroutines y Flow |
| **Lifecycle & ViewModel Compose** | `2.8.7` | Integracion de `ViewModel` y `collectAsStateWithLifecycle` |
| **Kotlinx Coroutines Android / Test** | `1.9.0` | Ejecucion asincrona y pruebas unitarias de flujos reactivos |
| **JUnit** | `4.13.2` | Ejecucion de pruebas unitarias de logica y CRUD |

---

## 5. Estado del Proyecto y Cumplimiento de Requisitos

| Funcionalidad Requerida | Estado | Implementacion Verificada |
| :--- | :--- | :--- |
| 1. Registrar tareas con titulo y descripcion | Completado | `TaskFormDialog` -> `TaskViewModel.saveTask()` -> `TaskDao.insertTask()` |
| 2. Consultar tareas almacenadas | Completado | `TaskDao.observeAllTasks()` (`Flow`) -> `ToDoScreen` (`LazyColumn`) |
| 3. Editar tareas existentes | Completado | `TaskViewModel.openEditTaskDialog()` -> `TaskDao.updateTask()` |
| 4. Marcar y desmarcar como completadas | Completado | `TaskViewModel.toggleTaskCompletion()` -> `TaskDao.updateCompletionStatus()` |
| 5. Eliminar tareas con confirmacion | Completado | `DeleteConfirmDialog` -> `TaskViewModel.confirmDeleteTask()` -> `TaskDao.deleteTask()` |
| 6. Mostrar pantalla Empty State | Completado | `EmptyStateView.kt` renderizado cuando la lista esta vacia |
| 7. Conservar registros al cerrar y abrir | Completado | Base de datos persistente en disco `todo_app_database.db` con Room SQLite |
| 8. Validar que el titulo no este vacio | Completado | Validacion en `TaskViewModel.saveTask()` y estado de error en `OutlinedTextField` |
