package com.angel.todoapp

import android.app.Application
import com.angel.todoapp.data.RoomTaskRepository
import com.angel.todoapp.data.TaskRepository
import com.angel.todoapp.data.ToDoDatabase

/**
 * Clase Application que inicializa y mantiene la instancia unica de Room Database
 * y del repositorio de tareas durante todo el ciclo de vida de la aplicacion.
 */
class ToDoApplication : Application() {

    val database: ToDoDatabase by lazy {
        ToDoDatabase.getInstance(this)
    }

    val repository: TaskRepository by lazy {
        RoomTaskRepository(database.taskDao())
    }
}
