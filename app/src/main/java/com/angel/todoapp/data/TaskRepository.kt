package com.angel.todoapp.data

import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de tareas para desacoplar la fuente de datos (Room)
 * de la capa de logica de negocio (ViewModel), facilitando pruebas unitarias.
 */
interface TaskRepository {
    val allTasks: Flow<List<TaskEntity>>
    suspend fun getTaskById(taskId: Long): TaskEntity?
    suspend fun addTask(title: String, description: String): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun toggleTaskCompletion(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
}

/**
 * Implementacion del repositorio que delega las operaciones de persistencia al DAO de Room.
 */
class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {

    override val allTasks: Flow<List<TaskEntity>> = taskDao.observeAllTasks()

    override suspend fun getTaskById(taskId: Long): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    override suspend fun addTask(title: String, description: String): Long {
        val now = System.currentTimeMillis()
        val entity = TaskEntity(
            title = title.trim(),
            description = description.trim(),
            isCompleted = false,
            createdAt = now,
            updatedAt = now
        )
        return taskDao.insertTask(entity)
    }

    override suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(
            title = task.title.trim(),
            description = task.description.trim(),
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    override suspend fun toggleTaskCompletion(task: TaskEntity) {
        taskDao.updateCompletionStatus(
            taskId = task.id,
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }
}
