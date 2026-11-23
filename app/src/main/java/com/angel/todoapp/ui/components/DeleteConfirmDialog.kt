package com.angel.todoapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.angel.todoapp.data.TaskEntity

/**
 * Dialogo de confirmacion obligatorio antes de eliminar una tarea de la base de datos Room.
 */
@Composable
fun DeleteConfirmDialog(
    task: TaskEntity,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = "Advertencia",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Confirmar eliminación",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar la tarea \"${task.title}\"? Esta acción borrará el registro permanentemente del dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
