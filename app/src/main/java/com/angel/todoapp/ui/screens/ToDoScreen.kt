package com.angel.todoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angel.todoapp.ui.components.DeleteConfirmDialog
import com.angel.todoapp.ui.components.EmptyStateView
import com.angel.todoapp.ui.components.TaskFormDialog
import com.angel.todoapp.ui.components.TaskItemCard
import com.angel.todoapp.ui.theme.EmeraldPrimary
import com.angel.todoapp.ui.theme.EmeraldPrimaryContainer
import com.angel.todoapp.ui.theme.NeutralSurface
import com.angel.todoapp.viewmodel.TaskFilter
import com.angel.todoapp.viewmodel.TaskViewModel

/**
 * Pantalla principal de la aplicacion ToDoApp construida con Jetpack Compose.
 * Muestra el resumen de progreso, selectores de filtro, listado reactivo de tarjetas
 * o pantalla Empty State cuando no existen tareas, e integra los dialogos de formulario y eliminacion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra notificaciones Snackbar cuando se completa una operacion CRUD
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.FactCheck,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ToDoApp",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gestión local de tareas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewTaskDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Registrar nueva tarea"
                    )
                },
                text = { Text("Nueva tarea") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tarjeta superior de resumen de tareas (Total, Pendientes, Completadas)
            SummaryHeaderCard(
                totalCount = uiState.totalCount,
                pendingCount = uiState.pendingCount,
                completedCount = uiState.completedCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Barra de filtros por estado
            FilterChipsRow(
                selectedFilter = uiState.filter,
                onFilterSelected = { viewModel.onFilterChange(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Contenido principal: Indicador de carga, Empty State o Lista de tarjetas
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.filteredTasks.isEmpty() -> {
                    EmptyStateView(
                        filter = uiState.filter,
                        onCreateTaskClick = { viewModel.openNewTaskDialog() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredTasks,
                            key = { task -> task.id }
                        ) { task ->
                            TaskItemCard(
                                task = task,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                                onEditClick = { viewModel.openEditTaskDialog(it) },
                                onDeleteClick = { viewModel.requestDeleteConfirmation(it) }
                            )
                        }
                    }
                }
            }
        }

        // Dialogo para crear o editar una tarea
        if (uiState.isFormDialogVisible) {
            TaskFormDialog(
                isEditing = uiState.editingTask != null,
                titleInput = uiState.titleInput,
                descriptionInput = uiState.descriptionInput,
                titleError = uiState.titleError,
                onTitleChange = { viewModel.onTitleChange(it) },
                onDescriptionChange = { viewModel.onDescriptionChange(it) },
                onConfirm = { viewModel.saveTask() },
                onDismiss = { viewModel.dismissFormDialog() }
            )
        }

        // Dialogo para confirmar la eliminacion de una tarea
        uiState.taskPendingDeletion?.let { taskToDelete ->
            DeleteConfirmDialog(
                task = taskToDelete,
                onConfirmDelete = { viewModel.confirmDeleteTask() },
                onDismiss = { viewModel.cancelDeleteConfirmation() }
            )
        }
    }
}

/**
 * Panel de metricas en tonos verdes que resume las tareas registradas en Room.
 */
@Composable
private fun SummaryHeaderCard(
    totalCount: Int,
    pendingCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                label = "Total",
                value = totalCount.toString()
            )
            StatItem(
                icon = Icons.Outlined.PendingActions,
                label = "Pendientes",
                value = pendingCount.toString()
            )
            StatItem(
                icon = Icons.Outlined.CheckCircleOutline,
                label = "Completadas",
                value = completedCount.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeutralSurface.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeutralSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = NeutralSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = NeutralSurface.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Fila de chips interactivos para filtrar tareas por estado.
 */
@Composable
private fun FilterChipsRow(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
