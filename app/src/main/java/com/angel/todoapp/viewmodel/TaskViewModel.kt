package com.angel.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angel.todoapp.data.TaskEntity
import com.angel.todoapp.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel principal que gestiona la logica de negocio de ToDoApp bajo el patron MVVM.
 * Conecta el flujo reactivo de Room Database (via TaskRepository) con el estado de Jetpack Compose.
 */
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    /**
     * Suscribe el ViewModel al Flow de tareas almacenadas en SQLite.
     * Cualquier insercion, edicion o eliminacion actualiza automaticamente el estado de la UI.
     */
    private fun observeTasks() {
        viewModelScope.launch {
            repository.allTasks.collect { storedTasks ->
                _uiState.update { current ->
                    current.copy(
                        tasks = storedTasks,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Cambia el filtro de visualizacion activo (Todas, Pendientes, Completadas).
     */
    fun onFilterChange(newFilter: TaskFilter) {
        _uiState.update { it.copy(filter = newFilter) }
    }

    /**
     * Abre el dialogo modal para registrar una nueva tarea limpia.
     */
    fun openNewTaskDialog() {
        _uiState.update {
            it.copy(
                isFormDialogVisible = true,
                editingTask = null,
                titleInput = "",
                descriptionInput = "",
                titleError = null
            )
        }
    }

    /**
     * Abre el dialogo modal precargando los datos de una tarea existente para su edicion.
     */
    fun openEditTaskDialog(task: TaskEntity) {
        _uiState.update {
            it.copy(
                isFormDialogVisible = true,
                editingTask = task,
                titleInput = task.title,
                descriptionInput = task.description,
                titleError = null
            )
        }
    }

    /**
     * Cierra el dialogo de registro o edicion sin aplicar cambios.
     */
    fun dismissFormDialog() {
        _uiState.update {
            it.copy(
                isFormDialogVisible = false,
                editingTask = null,
                titleInput = "",
                descriptionInput = "",
                titleError = null
            )
        }
    }

    /**
     * Actualiza el valor del campo titulo y limpia errores de validacion previos.
     */
    fun onTitleChange(newTitle: String) {
        _uiState.update {
            it.copy(
                titleInput = newTitle,
                titleError = if (newTitle.isNotBlank()) null else it.titleError
            )
        }
    }

    /**
     * Actualiza el valor del campo descripcion en el formulario.
     */
    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(descriptionInput = newDescription) }
    }

    /**
     * Valida que el titulo no este vacio y guarda la tarea (nueva o editada) en Room Database.
     */
    fun saveTask() {
        val currentState = _uiState.value
        val cleanTitle = currentState.titleInput.trim()
        val cleanDescription = currentState.descriptionInput.trim()

        if (cleanTitle.isEmpty()) {
            _uiState.update {
                it.copy(titleError = "El título de la tarea es obligatorio y no puede estar vacío.")
            }
            return
        }

        viewModelScope.launch {
            val editing = currentState.editingTask
            if (editing == null) {
                repository.addTask(title = cleanTitle, description = cleanDescription)
                _uiState.update {
                    it.copy(
                        isFormDialogVisible = false,
                        titleInput = "",
                        descriptionInput = "",
                        titleError = null,
                        userMessage = "Tarea registrada correctamente."
                    )
                }
            } else {
                repository.updateTask(
                    editing.copy(
                        title = cleanTitle,
                        description = cleanDescription
                    )
                )
                _uiState.update {
                    it.copy(
                        isFormDialogVisible = false,
                        editingTask = null,
                        titleInput = "",
                        descriptionInput = "",
                        titleError = null,
                        userMessage = "Tarea actualizada correctamente."
                    )
                }
            }
        }
    }

    /**
     * Alterna el estado de completado de una tarea y notifica al usuario.
     */
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            val feedback = if (!task.isCompleted) {
                "Tarea marcada como completada."
            } else {
                "Tarea marcada como pendiente."
            }
            _uiState.update { it.copy(userMessage = feedback) }
        }
    }

    /**
     * Solicita confirmacion antes de eliminar una tarea.
     */
    fun requestDeleteConfirmation(task: TaskEntity) {
        _uiState.update { it.copy(taskPendingDeletion = task) }
    }

    /**
     * Cancela el dialogo de confirmacion de eliminacion.
     */
    fun cancelDeleteConfirmation() {
        _uiState.update { it.copy(taskPendingDeletion = null) }
    }

    /**
     * Ejecuta la eliminacion definitiva de la tarea seleccionada en Room Database.
     */
    fun confirmDeleteTask() {
        val taskToDelete = _uiState.value.taskPendingDeletion ?: return
        viewModelScope.launch {
            repository.deleteTask(taskToDelete)
            _uiState.update {
                it.copy(
                    taskPendingDeletion = null,
                    userMessage = "Tarea eliminada permanentemente."
                )
            }
        }
    }

    /**
     * Limpia el mensaje temporal una vez mostrado en pantalla.
     */
    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

/**
 * Factory para instanciar TaskViewModel inyectando el repositorio de Room.
 */
class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
