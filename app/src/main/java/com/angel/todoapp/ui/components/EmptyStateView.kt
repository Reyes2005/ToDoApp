package com.angel.todoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.angel.todoapp.ui.theme.EmeraldPrimaryContainer
import com.angel.todoapp.viewmodel.TaskFilter

/**
 * Componente Empty State que se muestra cuando no existen tareas registradas
 * o cuando el filtro activo no arroja coincidencias.
 */
@Composable
fun EmptyStateView(
    filter: TaskFilter,
    onCreateTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (filter) {
        TaskFilter.ALL -> "Aún no tienes tareas registradas"
        TaskFilter.PENDING -> "No tienes tareas pendientes"
        TaskFilter.COMPLETED -> "Aún no has completado tareas"
    }

    val subtitle = when (filter) {
        TaskFilter.ALL -> "Organiza tu día agregando tu primera actividad con título y descripción. Todo se guardará localmente en tu dispositivo."
        TaskFilter.PENDING -> "¡Excelente trabajo! Has completado todas tus actividades registradas."
        TaskFilter.COMPLETED -> "Marca tus tareas pendientes con el círculo de verificación cuando las termines."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(EmeraldPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Checklist,
                contentDescription = "Sin tareas",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (filter == TaskFilter.ALL) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateTaskClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddTask,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Crear primera tarea")
            }
        }
    }
}
