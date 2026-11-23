# ToDoApp — Gestion Local de Tareas en Android

Aplicacion Android nativa desarrollada en **Kotlin** utilizando **Jetpack Compose**, **Room Database (SQLite)**, arquitectura **MVVM**, patron **Repository**, **Coroutines** y **Flow**.

## Caracteristicas Principales

1. **Registro de tareas**: Creacion de actividades con titulo obligatorio y descripcion opcional.
2. **Consulta reactiva**: Visualizacion en tiempo real mediante `Flow` y tarjetas Material 3, con resumen de metricas (Total, Pendientes, Completadas) y filtros por estado.
3. **Edicion de tareas**: Modificacion de titulo y descripcion de cualquier registro existente.
4. **Control de avance**: Marcado y desmarcado de tareas como completadas con actualizacion inmediata en SQLite.
5. **Eliminacion con confirmacion**: Dialogo de seguridad antes de borrar permanentemente un registro.
6. **Pantalla Empty State**: Vista informativa dedicada cuando no existen tareas registradas.
7. **Persistencia local**: Almacenamiento permanente en el dispositivo mediante Room (`todo_app_database.db`).
8. **Validacion de formulario**: Verificacion de titulo no vacio con retroalimentacion visual en espanol.

## Arquitectura del Codigo

* `app/src/main/java/com/angel/todoapp/data`:
  * `TaskEntity.kt`, `TaskDao.kt`, `ToDoDatabase.kt`, `TaskRepository.kt`.
* `app/src/main/java/com/angel/todoapp/viewmodel`:
  * `TaskUiState.kt`, `TaskViewModel.kt`.
* `app/src/main/java/com/angel/todoapp/ui`:
  * `theme/` (`Color.kt`, `Type.kt`, `Theme.kt`), `components/` (`TaskItemCard.kt`, `EmptyStateView.kt`, `TaskFormDialog.kt`, `DeleteConfirmDialog.kt`), `screens/` (`ToDoScreen.kt`).
* `app/src/main/java/com/angel/todoapp/`:
  * `ToDoApplication.kt`, `MainActivity.kt`.

Para mas detalles arquitectonicos consulte el archivo [`PROJECT_CONTEXT.md`](./PROJECT_CONTEXT.md).

## Compilacion y Pruebas desde Terminal

### 1. Compilar el APK Debug
```powershell
.\gradlew.bat assembleDebug
```
El APK instalable se genera en `app/build/outputs/apk/debug/app-debug.apk` y se copia en la raiz como `ToDoApp.apk`.

### 2. Ejecutar las Pruebas Unitarias (CRUD y MVVM)
```powershell
.\gradlew.bat testDebugUnitTest
```

### 3. Instalar en un Dispositivo o Emulador Conectado
```powershell
.\gradlew.bat installDebug
```

## Evidencias

En la carpeta [`Evidencias/`](./Evidencias) se incluyen las capturas de las funcionalidades implementadas y de la verificacion de compilacion y pruebas:
* `01_empty_state.png`: Pantalla Empty State cuando no hay tareas registradas.
* `02_validacion_titulo_vacio.png`: Validacion del campo titulo obligatorio en el formulario.
* `03_listado_y_completado.png`: Consulta de tarjetas de tareas y marcado de tarea completada.
* `04_edicion_tarea.png`: Dialogo de edicion con datos precargados.
* `05_confirmacion_eliminacion.png`: Dialogo de confirmacion previa a la eliminacion.
* `06_compilacion_y_tests.png`: Evidencia de compilacion con Gradle, generacion de codigo Room KSP y ejecucion de pruebas unitarias CRUD.
