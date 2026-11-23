package com.angel.todoapp.viewmodel

import com.angel.todoapp.data.TaskEntity

/**
 * Filtros disponibles para visualizar las tareas en la pantalla principal.
 */
enum class TaskFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

/**
 * Estado inmutable de la interfaz de usuario para la gestion de tareas.
 * Centraliza la lista de tareas, estadisticas, estado de los dialogos de formulario
 * y confirmacion de eliminacion, asi como mensajes de validacion y retroalimentacion.
 */
data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = true,

    // Estado del dialogo de creacion / edicion
    val isFormDialogVisible: Boolean = false,
    val editingTask: TaskEntity? = null,
    val titleInput: String = "",
    val descriptionInput: String = "",
    val titleError: String? = null,

    // Estado del dialogo de confirmacion de eliminacion
    val taskPendingDeletion: TaskEntity? = null,

    // Mensaje informativo temporal (Snackbar)
    val userMessage: String? = null
) {
    /**
     * Lista de tareas filtrada segun la seleccion activa del usuario.
     */
    val filteredTasks: List<TaskEntity>
        get() = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }

    val totalCount: Int
        get() = tasks.size

    val completedCount: Int
        get() = tasks.count { it.isCompleted }

    val pendingCount: Int
        get() = tasks.count { !it.isCompleted }
}
