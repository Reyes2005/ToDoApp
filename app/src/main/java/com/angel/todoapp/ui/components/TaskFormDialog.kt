package com.angel.todoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialogo modal reutilizable para registrar una tarea nueva o editar una existente.
 * Incluye validacion visual para impedir que el titulo quede vacio.
 */
@Composable
fun TaskFormDialog(
    isEditing: Boolean,
    titleInput: String,
    descriptionInput: String,
    titleError: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isEditing) "Editar tarea" else "Nueva tarea",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = onTitleChange,
                    label = { Text("Título de la tarea *") },
                    placeholder = { Text("Ej. Entregar reporte de laboratorio") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Title,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (titleError != null) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error de validación",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = titleError != null,
                    supportingText = {
                        if (titleError != null) {
                            Text(
                                text = titleError,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Campo obligatorio")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Agrega detalles, notas o pasos a seguir...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isEditing) "Guardar cambios" else "Registrar tarea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
