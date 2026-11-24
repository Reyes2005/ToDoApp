# Manual Técnico y Guía de Desarrollo en Kotlin — ToDoApp

Este documento es una guía completa desde cero para entender **qué se hizo, cómo se hizo y por qué se hizo** en la aplicación **ToDoApp**. Está dividido en cuatro partes:
1. **Manual de Sintaxis de Kotlin desde Cero** (para entender cada palabra clave usada en el código).
2. **Tecnologías Utilizadas** (qué es Jetpack Compose, Room, Coroutines y Flow, y por qué se usan).
3. **Arquitectura MVVM + Repository** (cómo viajan los datos desde la pantalla hasta la base de datos SQLite).
4. **Recorrido Archivo por Archivo** (explicación práctica de cómo funciona cada parte de la aplicación).

---

## Parte 1: Manual de Sintaxis de Kotlin desde Cero

Si vienes de otro lenguaje o estás empezando en Kotlin, estos son los bloques fundamentales con los que está construida toda la aplicación:

### 1.1. Variables: `val` vs `var`
En Kotlin existen dos palabras clave para almacenar datos:
* **`val` (Inmutable / Solo lectura):** Una vez que le asignas un valor, no puede cambiar. Es equivalente a una constante (`final` en Java). Se usa en la mayoría de los casos por seguridad.
  ```kotlin
  val nombreApp: String = "ToDoApp"
  val maxTareas = 100 // Inferencia de tipos: Kotlin detecta que es un entero (Int) automáticamente
  ```
* **`var` (Mutable / Variable):** Su valor puede modificarse posteriormente.
  ```kotlin
  var contador = 0
  contador = 1 // Válido porque se declaró con var
  ```

### 1.2. Tipos de Datos Básicos y Seguridad contra Nulos (`?`)
* `String`: Cadena de texto (`"Hola"`).
* `Int` y `Long`: Números enteros (`Int` para enteros estándar de 32 bits; `Long` para enteros de 64 bits como identificadores de base de datos o marcas de tiempo en milisegundos, ej. `0L`).
* `Boolean`: Valor lógico verdadero o falso (`true` / `false`).
* **Seguridad contra Nulos (`?`):** En Kotlin, ninguna variable puede almacenar `null` a menos que se declare explícitamente con el operador `?` al final del tipo. Esto previene errores de ejecución como `NullPointerException`.
  ```kotlin
  val titulo: String = "Comprar leche" // Nunca puede ser null
  val errorTitulo: String? = null      // Sí puede ser null (cuando no existe error de validación)
  ```

### 1.3. Funciones (`fun`) y Parámetros por Defecto
Las funciones se declaran con la palabra clave `fun`. Pueden recibir parámetros tipados y retornar un valor:
```kotlin
fun sumar(a: Int, b: Int = 10): Int {
    return a + b
}
```
* Si se omite el segundo parámetro (`sumar(5)`), Kotlin utiliza el valor por defecto (`10`).
* También es posible invocar funciones indicando el nombre de cada argumento (**Named Arguments**), práctica estándar en Jetpack Compose para mejorar la legibilidad:
  ```kotlin
  sumar(a = 5, b = 20)
  ```

### 1.4. Clases de Datos (`data class`) y el Método `.copy()`
Para representar modelos de datos (como una **Tarea** o el **Estado de la Interfaz**), Kotlin ofrece `data class`:
```kotlin
data class TaskEntity(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false
)
```
**¿Por qué se utiliza `data class`?**
Porque el compilador genera automáticamente `equals()`, `hashCode()`, `toString()` y el método **`.copy()`**. Como las propiedades son inmutables (`val`), cuando necesitamos modificar un campo (por ejemplo, marcar una tarea como completada), creamos una nueva instancia copiando los demás valores:
```kotlin
val tareaCompletada = tareaOriginal.copy(isCompleted = true)
```

### 1.5. Interfaces (`interface`)
Una `interface` define un **contrato**: especifica *qué operaciones están disponibles*, sin imponer *cómo se implementan internamente*.
* En `TaskDao.kt` y `TaskRepository.kt` utilizamos interfaces para declarar operaciones como `insertTask(...)`, `updateTask(...)` o `deleteTask(...)`. De este modo, Room genera la implementación SQL real y el `ViewModel` permanece desacoplado.

### 1.6. Condicionales y Expresión `when`
Además de la estructura condicional `if / else`, Kotlin incorpora **`when`**, que evalúa múltiples ramas y puede retornar un valor directamente:
```kotlin
val textoEstado = when (filter) {
    TaskFilter.ALL -> "Todas las tareas"
    TaskFilter.PENDING -> "Tareas pendientes"
    TaskFilter.COMPLETED -> "Tareas completadas"
}
```

### 1.7. Expresiones Lambda `{ }` (Funciones como Parámetros)
En Kotlin es posible pasar bloques de código (`{ ... }`) como argumentos a otras funciones. Este mecanismo se usa en todos los eventos de interfaz y operaciones sobre listas:
```kotlin
Button(onClick = { viewModel.saveTask() }) {
    Text("Guardar")
}
```
* `onClick = { ... }` indica la acción que se ejecutará cuando el usuario pulse el botón.
* Cuando una expresión lambda recibe un único parámetro, Kotlin permite referenciarlo implícitamente con **`it`**:
  ```kotlin
  tasks.filter { !it.isCompleted } // Filtra las tareas donde 'it' (cada tarea) no esté completada
  ```

### 1.8. Funciones `suspend` (Coroutines)
Las funciones precedidas por el modificador **`suspend`** indican operaciones asíncronas:
```kotlin
suspend fun insertTask(task: TaskEntity): Long
```
* **¿Por qué se usan?** Las operaciones de lectura y escritura en disco (SQLite) toman tiempo y no deben ejecutarse en el hilo principal de la interfaz. El modificador `suspend` permite pausar y reanudar la tarea en un hilo secundario sin congelar la pantalla.

---

## Parte 2: Tecnologías Utilizadas (¿Qué son y por qué se eligieron?)

### 2.1. Jetpack Compose (Interfaz Gráfica Declarativa)
* **¿Qué es?** Es el kit de herramientas moderno de Android para construir interfaces nativas directamente en Kotlin, prescindiendo de archivos XML de vistas.
* **¿Cómo funciona?** Se basa en funciones anotadas con **`@Composable`**. En lugar de mutar vistas manualmente con `findViewById`, la interfaz es una función del estado actual (`uiState`): cada vez que `uiState` cambia, Compose recompone únicamente los elementos visuales afectados.
* **¿Por qué se eligió?** Reduce drásticamente el código repetitivo, evita inconsistencias visuales y ofrece componentes oficiales de **Material Design 3** (`Scaffold`, `Card`, `FilterChip`, `AlertDialog`, `OutlinedTextField`).

### 2.2. Room Database sobre SQLite (Persistencia Local)
* **¿Qué es?** SQLite es el motor de base de datos relacional integrado en Android. **Room** es la capa de abstracción oficial sobre SQLite.
* **¿Por qué usar Room en lugar de SQLite directo?**
  1. Valida la sintaxis y tablas de las consultas SQL en tiempo de compilación (mediante KSP).
  2. Mapea automáticamente los registros SQL a objetos `TaskEntity` de Kotlin.
  3. Soporta `Flow` y funciones `suspend` de forma nativa.
* **Componentes de Room en el proyecto:**
  1. **`@Entity` (`TaskEntity.kt`):** Define el esquema de la tabla `tasks`.
  2. **`@Dao` (`TaskDao.kt`):** Define las consultas y transacciones SQL (`@Query`, `@Insert`, `@Update`, `@Delete`).
  3. **`@Database` (`ToDoDatabase.kt`):** Gestiona la conexión con el archivo físico `todo_app_database.db`.

### 2.3. Kotlin Coroutines (Ejecución Asíncrona)
* **¿Qué problema resuelven?** Android bloquea cualquier intento de acceso a base de datos en el hilo principal (*Main Thread*) para evitar que la interfaz deje de responder.
* **¿Cómo se aplican?** El `TaskViewModel` lanza operaciones dentro de `viewModelScope.launch { ... }`, delegando en Room la ejecución segura en hilos de entrada/salida (I/O).

### 2.4. Kotlin Flow y StateFlow (Flujo Reactivo de Datos)
* **¿Qué es `Flow`?** Es un flujo de datos asíncrono que emite valores secuencialmente. En `TaskDao`, la consulta `observeAllTasks()` retorna `Flow<List<TaskEntity>>`.
* **¿Por qué se utiliza?** Cada vez que se inserta, edita, completa o elimina una tarea en SQLite, Room emite automáticamente la nueva lista a través del `Flow`. El `TaskViewModel` actualiza su `StateFlow<TaskUiState>` y Jetpack Compose actualiza la pantalla en tiempo real.

---

## Parte 3: Arquitectura MVVM + Repository

Para mantener el código organizado, escalable y fácil de probar, se implementó la arquitectura **MVVM (Model - View - ViewModel)** complementada con el patrón **Repository**:

```text
[ Usuario ]
    │  (1) Interactúa con la pantalla (pulsa botón o escribe texto)
    ▼
[ VIEW: Jetpack Compose (ToDoScreen y Componentes UI) ]
    │  (2) Invoca un método del ViewModel (ej. viewModel.saveTask())
    ▼
[ VIEWMODEL: TaskViewModel + TaskUiState ]
    │  (3) Ejecuta validaciones (ej. título no vacío)
    │      y lanza una Coroutine hacia el Repository
    ▼
[ REPOSITORY: TaskRepository / RoomTaskRepository ]
    │  (4) Normaliza los datos y llama al DAO
    ▼
[ DATA (Room SQLite): TaskDao -> ToDoDatabase (todo_app_database.db) ]
    │  (5) Persiste el cambio en disco y emite la lista actualizada por Flow
    └──────────────────────► Actualiza StateFlow en ViewModel ──► Recompone la UI
```

### Separación de Responsabilidades
1. **Paquete `data` (Model + Repository):** Gestiona exclusivamente el almacenamiento SQLite. No conoce elementos de interfaz ni estados de pantalla.
2. **Paquete `viewmodel` (ViewModel + UiState):** Contiene la lógica de presentación y validación, sobreviviendo a cambios de configuración (como rotaciones de pantalla).
3. **Paquete `ui` (View):** Renderiza el estado expuesto por el `ViewModel` y notifica las acciones del usuario.

---

## Parte 4: Recorrido Paso a Paso por los Archivos del Proyecto

### 4.1. Capa de Datos (`app/src/main/java/com/angel/todoapp/data/`)

1. **`TaskEntity.kt`:**
   * Declara `@Entity(tableName = "tasks")`.
   * Incluye `@PrimaryKey(autoGenerate = true) val id: Long = 0L`, además de `title`, `description`, `isCompleted`, `createdAt` y `updatedAt`.
2. **`TaskDao.kt`:**
   * **`observeAllTasks()`:** Ejecuta `SELECT * FROM tasks ORDER BY is_completed ASC, updated_at DESC` y retorna un `Flow<List<TaskEntity>>` para mostrar primero las tareas pendientes y luego las completadas.
   * **`insertTask()` / `updateTask()` / `updateCompletionStatus()` / `deleteTask()`:** Operaciones `suspend` para completar el ciclo CRUD.
3. **`ToDoDatabase.kt`:**
   * Implementa `RoomDatabase` con patrón Singleton (`@Volatile` y `synchronized`) sobre el archivo `todo_app_database.db`, garantizando una única instancia activa en memoria.
4. **`TaskRepository.kt`:**
   * Define la interfaz `TaskRepository` y la clase `RoomTaskRepository`, limpiando espacios en blanco (`.trim()`) y actualizando las marcas de tiempo (`System.currentTimeMillis()`) en cada escritura.

### 4.2. Capa de Lógica (`app/src/main/java/com/angel/todoapp/viewmodel/`)

5. **`TaskUiState.kt`:**
   * Modela el estado completo de la pantalla: lista de tareas, filtro activo (`ALL`, `PENDING`, `COMPLETED`), contadores (`totalCount`, `pendingCount`, `completedCount`), visibilidad del formulario modal, mensaje de error de validación (`titleError`) y tarea pendiente de confirmación de borrado (`taskPendingDeletion`).
6. **`TaskViewModel.kt`:**
   * Se suscribe en su bloque `init` al flujo `repository.allTasks`.
   * En `saveTask()` verifica que `titleInput.trim()` no esté vacío; si está vacío, asigna el mensaje `"El título de la tarea es obligatorio y no puede estar vacío."` a `titleError` e impide la escritura en base de datos.
   * Gestiona la edición, el cambio de estado completado/pendiente y la confirmación previa a la eliminación.

### 4.3. Capa de Interfaz (`app/src/main/java/com/angel/todoapp/ui/`)

7. **`ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`):**
   * Configura el esquema Material 3 con tonos verdes (`EmeraldPrimary #1B6B4A`, `EmeraldPrimaryContainer #D6F2E3`, `SageSecondary #4C6357`) y fondos neutros (`NeutralBackground #F6F8F6`).
8. **`ui/components/`:**
   * **`TaskItemCard.kt`:** Tarjeta de cada tarea con indicador circular de completado, etiqueta de estado (`Pendiente` / `Completada`), fecha de actualización y botones para editar y eliminar.
   * **`EmptyStateView.kt`:** Vista Empty State que se muestra automáticamente cuando no existen tareas registradas.
   * **`TaskFormDialog.kt`:** Diálogo modal para registrar o editar tareas con validación visual en el campo de título.
   * **`DeleteConfirmDialog.kt`:** Diálogo de confirmación antes de eliminar definitivamente una tarea.
9. **`ui/screens/ToDoScreen.kt`:**
   * Integra la barra superior (`TopAppBar`), la tarjeta de resumen de métricas (`SummaryHeaderCard`), los filtros (`FilterChipsRow`), el listado (`LazyColumn`) o `EmptyStateView`, el botón flotante (`ExtendedFloatingActionButton`) y el host de notificaciones (`SnackbarHost`).
10. **`ToDoApplication.kt` y `MainActivity.kt`:**
    * Inicializan la base de datos y el repositorio al arrancar la aplicación e inyectan el `TaskViewModel` en la jerarquía de Jetpack Compose.
