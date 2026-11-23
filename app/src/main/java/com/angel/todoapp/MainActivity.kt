package com.angel.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.angel.todoapp.ui.screens.ToDoScreen
import com.angel.todoapp.ui.theme.ToDoAppTheme
import com.angel.todoapp.viewmodel.TaskViewModel
import com.angel.todoapp.viewmodel.TaskViewModelFactory

/**
 * Punto de entrada principal de la aplicacion Android ToDoApp.
 * Configura el tema visual en Jetpack Compose e inyecta el repositorio de Room en el ViewModel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = application as ToDoApplication
        val viewModelFactory = TaskViewModelFactory(appContainer.repository)

        setContent {
            ToDoAppTheme {
                val taskViewModel: TaskViewModel = viewModel(factory = viewModelFactory)
                ToDoScreen(viewModel = taskViewModel)
            }
        }
    }
}
