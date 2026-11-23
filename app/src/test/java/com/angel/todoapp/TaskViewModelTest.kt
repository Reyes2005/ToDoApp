package com.angel.todoapp

import com.angel.todoapp.data.RoomTaskRepository
import com.angel.todoapp.data.TaskDao
import com.angel.todoapp.data.TaskEntity
import com.angel.todoapp.viewmodel.TaskFilter
import com.angel.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Implementacion de TaskDao respaldada por coleccion reactiva para verificar
 * el ciclo completo de operaciones CRUD, el repositorio y las reglas del ViewModel.
 */
class FakeTaskDao : TaskDao {
    private val items = mutableListOf<TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())
    private var autoId = 1L

    private fun emitSorted() {
        flow.value = items.sortedWith(
            compareBy<TaskEntity> { it.isCompleted }.thenByDescending { it.updatedAt }
        )
    }

    override fun observeAllTasks(): Flow<List<TaskEntity>> = flow.map { it }

    override suspend fun getTaskById(taskId: Long): TaskEntity? {
        return items.find { it.id == taskId }
    }

    override suspend fun insertTask(task: TaskEntity): Long {
        val assignedId = if (task.id == 0L) autoId++ else task.id
        val saved = task.copy(id = assignedId)
        items.removeAll { it.id == assignedId }
        items.add(saved)
        emitSorted()
        return assignedId
    }

    override suspend fun updateTask(task: TaskEntity) {
        val index = items.indexOfFirst { it.id == task.id }
        if (index != -1) {
            items[index] = task
            emitSorted()
        }
    }

    override suspend fun updateCompletionStatus(taskId: Long, isCompleted: Boolean, updatedAt: Long) {
        val index = items.indexOfFirst { it.id == taskId }
        if (index != -1) {
            items[index] = items[index].copy(isCompleted = isCompleted, updatedAt = updatedAt)
            emitSorted()
        }
    }

    override suspend fun deleteTask(task: TaskEntity) {
        items.removeAll { it.id == task.id }
        emitSorted()
    }
}

/**
 * Suite de pruebas unitarias para validar todas las funcionalidades requeridas en ToDoApp:
 * 1. Registro de tareas con titulo y descripcion.
 * 2. Consulta y estado Empty State inicial.
 * 3. Validacion de titulo no vacio.
 * 4. Edicion de tareas existentes.
 * 5. Marcado y desmarcado de tareas como completadas.
 * 6. Eliminacion con flujo de confirmacion previa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dao: FakeTaskDao
    private lateinit var repository: RoomTaskRepository
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeTaskDao()
        repository = RoomTaskRepository(dao)
        viewModel = TaskViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicial_sinTareas_muestraListaVaciaParaEmptyState() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.tasks.isEmpty())
        assertEquals(0, state.totalCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun validacionTituloVacio_impideGuardarYMuestraMensajeDeError() = runTest {
        viewModel.openNewTaskDialog()
        viewModel.onTitleChange("   ")
        viewModel.onDescriptionChange("Descripcion de prueba")
        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertTrue(state.isFormDialogVisible)
        assertNotNull(state.titleError)
        assertEquals(0, state.totalCount)
    }

    @Test
    fun registrarYConsultarTarea_almacenaCorrectamenteTituloYDescripcion() = runTest {
        viewModel.openNewTaskDialog()
        viewModel.onTitleChange("Implementar Room Database")
        viewModel.onDescriptionChange("Configurar Entity, DAO y Repository con Flow")
        viewModel.saveTask()

        val state = viewModel.uiState.value
        assertFalse(state.isFormDialogVisible)
        assertNull(state.titleError)
        assertEquals(1, state.totalCount)
        assertEquals("Implementar Room Database", state.tasks.first().title)
        assertEquals("Configurar Entity, DAO y Repository con Flow", state.tasks.first().description)
        assertFalse(state.tasks.first().isCompleted)
    }

    @Test
    fun editarTareaExistente_actualizaTituloYDescripcion() = runTest {
        // 1. Crear tarea inicial
        viewModel.openNewTaskDialog()
        viewModel.onTitleChange("Tarea original")
        viewModel.onDescriptionChange("Descripcion original")
        viewModel.saveTask()

        val createdTask = viewModel.uiState.value.tasks.first()

        // 2. Abrir edicion y modificar
        viewModel.openEditTaskDialog(createdTask)
        viewModel.onTitleChange("Tarea editada en MVVM")
        viewModel.onDescriptionChange("Descripcion actualizada")
        viewModel.saveTask()

        val updatedTask = viewModel.uiState.value.tasks.first()
        assertEquals(createdTask.id, updatedTask.id)
        assertEquals("Tarea editada en MVVM", updatedTask.title)
        assertEquals("Descripcion actualizada", updatedTask.description)
    }

    @Test
    fun marcarYDesmarcarTarea_actualizaEstadoCompletadoYFiltros() = runTest {
        viewModel.openNewTaskDialog()
        viewModel.onTitleChange("Probar filtros de estado")
        viewModel.saveTask()

        val task = viewModel.uiState.value.tasks.first()
        assertFalse(task.isCompleted)
        assertEquals(1, viewModel.uiState.value.pendingCount)
        assertEquals(0, viewModel.uiState.value.completedCount)

        // Marcar como completada
        viewModel.toggleTaskCompletion(task)
        val completedTask = viewModel.uiState.value.tasks.first()
        assertTrue(completedTask.isCompleted)
        assertEquals(0, viewModel.uiState.value.pendingCount)
        assertEquals(1, viewModel.uiState.value.completedCount)

        // Filtrar pendientes (debe estar vacio)
        viewModel.onFilterChange(TaskFilter.PENDING)
        assertTrue(viewModel.uiState.value.filteredTasks.isEmpty())

        // Filtrar completadas (debe contener 1)
        viewModel.onFilterChange(TaskFilter.COMPLETED)
        assertEquals(1, viewModel.uiState.value.filteredTasks.size)

        // Desmarcar como completada
        viewModel.toggleTaskCompletion(completedTask)
        val pendingAgain = viewModel.uiState.value.tasks.first()
        assertFalse(pendingAgain.isCompleted)
    }

    @Test
    fun eliminarTareaConConfirmacion_cancelaOEliminaSegunConfirmacion() = runTest {
        viewModel.openNewTaskDialog()
        viewModel.onTitleChange("Tarea por eliminar")
        viewModel.saveTask()

        val task = viewModel.uiState.value.tasks.first()

        // Solicitar eliminacion y luego cancelar
        viewModel.requestDeleteConfirmation(task)
        assertNotNull(viewModel.uiState.value.taskPendingDeletion)
        viewModel.cancelDeleteConfirmation()
        assertNull(viewModel.uiState.value.taskPendingDeletion)
        assertEquals(1, viewModel.uiState.value.totalCount)

        // Solicitar eliminacion y confirmar
        viewModel.requestDeleteConfirmation(task)
        viewModel.confirmDeleteTask()
        assertNull(viewModel.uiState.value.taskPendingDeletion)
        assertEquals(0, viewModel.uiState.value.totalCount)
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
    }
}
