# Manual Técnico y Guía de Desarrollo en Kotlin — ToDoApp

Este documento es una guía completa desde cero para entender **qué se hizo, cómo se hizo y por qué se hizo** en la aplicación **ToDoApp**. Está dividido en cuatro partes:
1. **Manual de Sintaxis de Kotlin desde Cero** (para entender cada palabra clave usada en el código).
2. **Tecnologías Utilizadas** (qué es Jetpack Compose, Room, Coroutines y Flow, y por qué se usan).
3. **Arquitectura MVVM + Repository** (cómo viajan los datos desde la pantalla hasta la base de datos SQLite).
4. **Recorrido Archivo por Archivo** (explicación práctica de cómo funciona cada parte de la app).

---

## Parte 1: Manual de Sintaxis de Kotlin desde Cero

Si vienes de otro lenguaje o estás empezando en Kotlin, estos son los bloques fundamentales con los que está construida toda la aplicación:

### 1.1. Variables: `val` vs `var`
En Kotlin existen dos palabras para guardar datos:
* **`val` (Inmutable / Solo lectura):** Una vez que le asignas un valor, no puede cambiar. Es equivalente a una constante (`final` en Java). Se usa el 90% de las veces por seguridad.
  ```kotlin
  val nombreApp: String = "ToDoApp"
  val maxTareas = 100 // Kotlin adivina que es un número entero (Int) automáticamente
  ```
* **`var` (Mutable / Variable):** Su valor puede cambiar después.
  ```kotlin
  var contador = 0
  contador = 1 // Válido porque se declaró con var
  ```

### 1.2. Tipos de Datos Básicos y Nulos (`?`)
* `String`: Texto (`"Hola"`).
* `Int` y `Long`: Números enteros (`Int` para números normales, `Long` para números grandes como IDs de base de datos o fechas en milisegundos, ej. `0L`).
* `Boolean`: Verdadero o falso (`true` / `false`).
* **Seguridad contra Nulos (`?`):** En Kotlin, ninguna variable puede ser nula (`null`) a menos que le pongas un signo de interrogación `?` al final del tipo. Esto evita que la app se cierre por el clásico error *NullPointerException*.
  ```kotlin
  val titulo: String = "Comprar leche" // Nunca puede ser null
  val errorTitulo: String? = null      // Sí puede ser null (cuando no hay error)
  ```

### 1.3. Funciones (`fun`) y Parámetros por Defecto
Las funciones se declaran con la palabra `fun`. Pueden recibir parámetros y devolver un resultado:
```kotlin
fun sumar(a: Int, b: Int = 10): Int {
    return a + b
}
```
* Si no pasas el segundo parámetro (`sumar(5)`), Kotlin usa el valor por defecto (`10`).
* También puedes llamar a los parámetros por su nombre (**Named Arguments**), muy usado en nuestras pantallas para que el código se lea claro:
  ```kotlin
  sumar(a = 5, b = 20)
  ```

### 1.4. Clases de Datos (`data class`) y el método `.copy()`
En una aplicación necesitamos representar objetos (como una **Tarea** o el **Estado de la Pantalla**). En lugar de escribir decenas de líneas, Kotlin tiene `data class`:
```kotlin
data class TaskEntity(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false
)
```
**¿Por qué usamos `data class`?**
Porque Kotlin le genera automáticamente un método llamado **`.copy()`**. Como nuestras variables son `val` (inmutables), cuando queremos cambiar un solo dato (por ejemplo, marcar una tarea como completada), creamos una copia idéntica cambiando solo ese campo:
```kotlin
val tareaCompletada = tareaOriginal.copy(isCompleted = true)
```

### 1.5. Interfaces (`interface`)
Una `interface` es un **contrato**: define *qué acciones existen*, pero sin decir *cómo se hacen por dentro*.
* En `TaskDao.kt` y `TaskRepository.kt` usamos interfaces para declarar funciones como `insertTask(...)` o `deleteTask(...)`. Así, Room o nuestro repositorio se encargan del trabajo pesado por detrás.

### 1.6. Condicionales y Expresión `when`
Además del `if / else` tradicional, Kotlin tiene **`when`** (una versión mucho más limpia del `switch` de otros lenguajes) que puede devolver un valor directamente:
```kotlin
val textoEstado = when (filter) {
    TaskFilter.ALL -> "Todas las tareas"
    TaskFilter.PENDING -> "Tareas pendientes"
    TaskFilter.COMPLETED -> "Tareas completadas"
}
```

### 1.7. Lambdas `{ }` (Funciones como Parámetros)
En Kotlin puedes pasar una acción (una función sin nombre encerrada entre llaves `{ }`) a otro componente. Esto es la base de todos los botones y pantallas:
```kotlin
Button(onClick = { viewModel.saveTask() }) {
    Text("Guardar")
}
```
* `onClick = { ... }` significa: *"Cuando el usuario toque este botón, ejecuta lo que está dentro de estas llaves"*.
* Cuando una lambda recibe un solo dato, Kotlin lo llama automáticamente **`it`**:
  ```kotlin
  tasks.filter { !it.isCompleted } // Filtra las tareas donde 'it' (cada tarea) NO esté completada
  ```

### 1.8. Funciones `suspend` (Coroutines)
Una función que lleva la palabra **`suspend`** antes de `fun`:
```kotlin
suspend fun insertTask(task: TaskEntity): Long
```
* **¿Qué significa?** Que es una operación que puede tardar unos milisegundos (como escribir en el disco duro/SQLite) y **no debe congelar la pantalla del teléfono**. Kotlin la pausa y la ejecuta en segundo plano sin trabar la interfaz.

---

## Parte 2: Tecnologías Utilizadas (¿Qué son y por qué se eligieron?)

Para que **ToDoApp** sea una aplicación Android moderna y profesional, se utilizaron las herramientas oficiales recomendadas hoy por Google:

### 2.1. Jetpack Compose (Interfaz Gráfica Declarativa)
* **¿Qué es?** Es la tecnología moderna de Android para construir pantallas directamente en código Kotlin, reemplazando a los antiguos archivos `.xml`.
* **¿Cómo funciona?** Se basa en funciones con la etiqueta **`@Composable`**. En vez de buscar botones con `findViewById` y cambiarles el texto a mano, en Compose tú dices: *"La pantalla se dibuja según el estado actual (`uiState`). Si `uiState.isFormDialogVisible` es `true`, dibuja el diálogo; si la lista está vacía, dibuja el Empty State"*.
* **¿Por qué se usó?** Menos código, cero desincronización entre lo que hay en memoria y lo que ve el usuario, y soporte nativo para **Material Design 3** (tarjetas, chips, iconos y animaciones).

### 2.2. Room Database sobre SQLite (Persistencia Local)
* **¿Qué es?** SQLite es el motor de base de datos que viene integrado dentro de todos los teléfonos Android. **Room** es la librería oficial de Google que se coloca encima de SQLite.
* **¿Por qué se usó Room en vez de SQLite crudo?**
  1. Verifica en tiempo de compilación que tus consultas SQL (`SELECT`, `UPDATE`) no tengan errores de escritura.
  2. Convierte automáticamente las filas de la tabla SQL en objetos Kotlin (`TaskEntity`).
  3. Se conecta directamente con `Flow` y `Coroutines`.
* **Los 3 pilares de Room en nuestro proyecto:**
  1. **`@Entity` (`TaskEntity.kt`):** Define el molde de la tabla `tasks` y sus columnas.
  2. **`@Dao` (`TaskDao.kt`):** Define las operaciones SQL (insertar, consultar, editar, borrar).
  3. **`@Database` (`ToDoDatabase.kt`):** Crea y administra el archivo físico `todo_app_database.db` dentro de la memoria interna del teléfono.

### 2.3. Kotlin Coroutines (Operaciones Asíncronas)
* **¿Qué problema resuelven?** En Android existe un hilo principal llamado **Main Thread (o UI Thread)** encargado de dibujar la pantalla a 60/120 cuadros por segundo. Android prohíbe leer o escribir bases de datos en ese hilo porque la pantalla se congelaría.
* **¿Cómo lo resolvemos?** Con **Coroutines** (`viewModelScope.launch { ... }` y funciones `suspend`). Permiten lanzar la escritura o borrado en SQLite en segundo plano y regresar el resultado a la pantalla sin bloquear al usuario.

### 2.4. Kotlin Flow y StateFlow (Observación Reactiva de Datos)
* **¿Qué es un `Flow`?** Imagina una tubería por donde viajan datos en vivo. En lugar de pedirle a la base de datos *"dame las tareas"* cada vez que insertas o borras algo, `TaskDao` devuelve un `Flow<List<TaskEntity>>`.
* **¿Por qué es tan útil?** Cada vez que se inserta, edita, completa o elimina una fila en la tabla `tasks` de SQLite, **Room empuja automáticamente la lista actualizada por la tubería `Flow`**, el `ViewModel` la recibe en su `StateFlow` y **Jetpack Compose redibuja la pantalla al instante**.

---

## Parte 3: Arquitectura MVVM + Repository

Una regla fundamental del desarrollo profesional es **no mezclar todo en un solo archivo**. Si pones las consultas SQL junto con el código que dibuja los botones, la app se vuelve imposible de mantener o probar.

Por eso implementamos la arquitectura **MVVM (Model - View - ViewModel)** con un **Repository**:

```text
[ Usuario ]
    │  (1) Toca un botón o escribe texto
    ▼
[ VIEW: Jetpack Compose (ToDoScreen / Componentes) ]
    │  (2) Llama a una función del ViewModel (ej. viewModel.saveTask())
    ▼
[ VIEWMODEL: TaskViewModel + TaskUiState ]
    │  (3) Valida los datos (ej. que el título no esté vacío)
    │      y lanza una Coroutine hacia el Repository
    ▼
[ REPOSITORY: TaskRepository / RoomTaskRepository ]
    │  (4) Prepara la entidad y llama al DAO
    ▼
[ DATA (Room SQLite): TaskDao -> ToDoDatabase (todo_app_database.db) ]
    │  (5) Guarda en disco y emite la nueva lista por el Flow
    └──────────────────────► Actualiza el StateFlow del ViewModel ──► Redibuja la Vista
```

### ¿Por qué existe cada capa?
1. **Capa `data` (Model + Repository):** Solo sabe hablar con SQLite. No tiene idea de qué colores tiene la pantalla ni qué botones existen.
2. **Capa `viewmodel` (ViewModel + UiState):** Es el "cerebro" de la pantalla. Recibe lo que el usuario quiere hacer, valida reglas (como impedir guardar un título vacío), coordina con el repositorio y mantiene el estado (`TaskUiState`) vivo aunque el usuario gire la pantalla del teléfono.
3. **Capa `ui` (View):** Es "tonta" a propósito: solo dibuja lo que dice `TaskUiState` y avisa al `ViewModel` cuando el usuario toca algo.

---

## Parte 4: Recorrido Paso a Paso por los Archivos del Proyecto

A continuación se explica qué hace cada archivo que creamos dentro de `app/src/main/java/com/angel/todoapp/`:

### 4.1. Capa de Datos (`data/`)

#### 1. `TaskEntity.kt` — La tabla SQLite
Define cómo se guarda una tarea en la base de datos:
* `@Entity(tableName = "tasks")`: Le indica a Room que cree una tabla llamada `tasks`.
* `@PrimaryKey(autoGenerate = true) val id: Long = 0L`: Crea un ID único autoincremental (1, 2, 3...) para cada tarea registrada.
* Guarda `title` (título), `description` (descripción), `isCompleted` (si ya se completó o no), `createdAt` y `updatedAt` (fechas en milisegundos).

#### 2. `TaskDao.kt` — Las operaciones SQL (CRUD)
Contiene las 4 operaciones básicas (**C**reate, **R**ead, **U**pdate, **D**elete):
* **Read (Consultar):**
  ```kotlin
  @Query("SELECT * FROM tasks ORDER BY is_completed ASC, updated_at DESC")
  fun observeAllTasks(): Flow<List<TaskEntity>>
  ```
  Ordena las tareas para que las **Pendientes (`is_completed = 0`) aparezcan arriba** y las completadas abajo, ordenadas de la más reciente a la más antigua.
* **Create (Registrar):** `@Insert suspend fun insertTask(task: TaskEntity): Long`
* **Update (Editar / Completar):** `@Update suspend fun updateTask(task: TaskEntity)` y `@Query("UPDATE tasks SET is_completed = ...")` para marcar o desmarcar rápido.
* **Delete (Eliminar):** `@Delete suspend fun deleteTask(task: TaskEntity)`

#### 3. `ToDoDatabase.kt` — El motor Room
Conecta `TaskEntity` con `TaskDao`.
* Usa el patrón **Singleton** (`@Volatile private var INSTANCE: ToDoDatabase? = null` y `synchronized(this)`).
* **¿Por qué Singleton?** Porque abrir una conexión a un archivo SQLite consume memoria. El Singleton garantiza que en toda la aplicación solo exista **una única conexión abierta** al archivo `todo_app_database.db`.

#### 4. `TaskRepository.kt` — El intermediario limpio
Define la interfaz `TaskRepository` y la clase `RoomTaskRepository`.
* Cuando el `ViewModel` pide agregar una tarea (`addTask(title, description)`), el repositorio limpia espacios sobrantes (`.trim()`), asigna la hora actual (`System.currentTimeMillis()`) y la manda al DAO.
* Además, al ser una interfaz, nos permitió crear un `FakeTaskDao` en las pruebas unitarias (`TaskViewModelTest.kt`) para probar toda la lógica sin necesitar un teléfono conectado.

---

### 4.2. Capa de Lógica y Estado (`viewmodel/`)

#### 5. `TaskUiState.kt` — La fotografía de la pantalla
Es una `data class` que contiene absolutamente todo lo que la pantalla necesita saber en un instante dado:
* `tasks`: La lista completa que viene de Room.
* `filter`: El filtro seleccionado (`ALL`, `PENDING`, `COMPLETED`).
* `filteredTasks`: Una propiedad calculada que devuelve las tareas según el filtro activo.
* `totalCount`, `pendingCount`, `completedCount`: Contadores automáticos para el panel verde superior.
* `isFormDialogVisible`, `editingTask`, `titleInput`, `descriptionInput`, `titleError`: Controlan si la ventana de "Nueva tarea / Editar tarea" está abierta, qué texto tiene escrito el usuario y si hay un error por dejar el título vacío.
* `taskPendingDeletion`: Guarda temporalmente la tarea que el usuario quiere borrar mientras está abierta la ventana de `"¿Estás seguro de eliminar?"`.

#### 6. `TaskViewModel.kt` — El controlador de eventos
* En su bloque `init { observeTasks() }`, empieza a escuchar el `Flow` de Room apenas se abre la app.
* **`saveTask()` (Validación + Guardado):**
  1. Toma `titleInput.trim()`.
  2. **Regla de negocio:** Si está vacío (`cleanTitle.isEmpty()`), **no guarda nada en Room** y actualiza `titleError = "El título de la tarea es obligatorio y no puede estar vacío."`.
  3. Si el título es válido, revisa si `editingTask == null`. Si es `null`, inserta una tarea nueva (`repository.addTask(...)`); si no es `null`, actualiza la tarea existente (`repository.updateTask(...)`).
* **`toggleTaskCompletion(task)`:** Invierte el valor `isCompleted` en la base de datos y muestra un mensaje `Snackbar` en español.
* **`requestDeleteConfirmation(task)` y `confirmDeleteTask()`:** Implementan el borrado en dos pasos para cumplir con la confirmación obligatoria antes de eliminar.

---

### 4.3. Capa de Interfaz de Usuario (`ui/`)

#### 7. `ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`)
Define la identidad visual solicitada:
* **Tonos verdes principales:** Verde esmeralda profundo (`#1B6B4A`) para barras principales, botones y acentos; verde salvia claro (`#D6F2E3`) para contenedores e iconos; y fondos neutros suaves (`#F6F8F6`) con tarjetas blancas (`#FFFFFF`) para que la lectura sea limpia y descansada.

#### 8. `ui/components/` — Piezas visuales independientes
En lugar de hacer una pantalla gigante, dividimos la interfaz en 4 piezas reutilizables:
* **`TaskItemCard.kt`:** Dibuja la tarjeta de cada tarea. Si la tarea está completada, tacha el texto (`TextDecoration.LineThrough`), cambia el borde a verde suave y muestra la etiqueta `"Completada"`. Incluye los tres botones de acción: el círculo izquierdo (completar/desmarcar), el lápiz (editar) y el bote de basura rojo (eliminar).
* **`EmptyStateView.kt`:** Cuando `filteredTasks.isEmpty()` es verdadero (no hay tareas en la base de datos), muestra un círculo verde con un icono de lista, un mensaje claro en español y el botón `"Crear primera tarea"`.
* **`TaskFormDialog.kt`:** Es la ventana emergente con los dos campos (`OutlinedTextField`) para Título y Descripción. Si `titleError` no es nulo, pinta el borde del campo en rojo y muestra el mensaje de validación debajo.
* **`DeleteConfirmDialog.kt`:** Es el cuadro de diálogo de advertencia que pregunta si realmente deseas borrar la tarea seleccionada, con los botones `"Cancelar"` y `"Eliminar"`.

#### 9. `ui/screens/ToDoScreen.kt` — La pantalla principal
Une todas las piezas dentro de un `Scaffold` de Material 3:
* **`topBar`**: Barra superior con el logo y título de `ToDoApp`.
* **`SummaryHeaderCard`**: Panel verde superior que muestra en vivo cuántas tareas hay en Total, Pendientes y Completadas.
* **`FilterChipsRow`**: Botones de filtro (`Todas`, `Pendientes`, `Completadas`).
* **`LazyColumn`**: Lista optimizada que solo dibuja en memoria las tarjetas visibles en pantalla.
* **`floatingActionButton`**: El botón flotante `"+ Nueva tarea"` en la esquina inferior derecha.

#### 10. `ToDoApplication.kt` y `MainActivity.kt` — El arranque de la App
* Cuando abres la app en Android, primero se ejecuta **`ToDoApplication`**, que prepara la instancia única de `ToDoDatabase` y `RoomTaskRepository`.
* Luego se abre **`MainActivity`**, que crea el `TaskViewModel` conectado a ese repositorio y lanza `ToDoScreen` dentro de `setContent { ToDoAppTheme { ... } }`.
* **¿Por qué se conservan los datos al cerrar y volver a abrir la app?** Porque Room guarda cada cambio inmediatamente en el archivo físico SQLite `/data/data/com.angel.todoapp/databases/todo_app_database.db` del dispositivo. Al reabrir la app, `observeAllTasks()` lee ese archivo y restaura todas tus tareas automáticamente.
