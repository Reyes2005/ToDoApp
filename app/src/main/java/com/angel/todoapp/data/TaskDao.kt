package com.angel.todoapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para ejecutar operaciones CRUD sobre la tabla "tasks".
 * Utiliza Flow para emitir actualizaciones en tiempo real cuando cambian los registros
 * y funciones suspend para ejecutar escrituras de forma asincrona mediante Coroutines.
 */
@Dao
interface TaskDao {

    /**
     * Consulta reactiva de todas las tareas almacenadas.
     * Ordena primero las tareas pendientes (is_completed = 0) y luego por fecha de actualizacion descendente.
     */
    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, updated_at DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    /**
     * Obtiene una tarea especifica por su identificador primario.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    /**
     * Registra una nueva tarea en la base de datos SQLite.
     * Retorna el ID de la fila insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * Actualiza los datos de una tarea existente (titulo, descripcion, estado o fecha).
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Actualiza unicamente el estado de completado de una tarea y su marca de tiempo.
     */
    @Query("UPDATE tasks SET is_completed = :isCompleted, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun updateCompletionStatus(taskId: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Elimina una tarea especifica de la base de datos.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
