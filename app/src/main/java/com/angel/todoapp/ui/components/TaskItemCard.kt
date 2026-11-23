package com.angel.todoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.angel.todoapp.data.TaskEntity
import com.angel.todoapp.ui.theme.CompletedBadgeBg
import com.angel.todoapp.ui.theme.CompletedBadgeText
import com.angel.todoapp.ui.theme.DangerRed
import com.angel.todoapp.ui.theme.EmeraldPrimary
import com.angel.todoapp.ui.theme.NeutralOutline
import com.angel.todoapp.ui.theme.NeutralSurface
import com.angel.todoapp.ui.theme.PendingBadgeBg
import com.angel.todoapp.ui.theme.PendingBadgeText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta visual que representa una tarea individual dentro del listado principal.
 * Permite marcar o desmarcar la tarea como completada, abrir el formulario de edicion
 * o solicitar la confirmacion de eliminacion.
 */
@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditClick: (TaskEntity) -> Unit,
    onDeleteClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorderColor = if (task.isCompleted) {
        EmeraldPrimary.copy(alpha = 0.35f)
    } else {
        NeutralOutline
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = NeutralSurface
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Boton para marcar o desmarcar la tarea como completada
            IconButton(
                onClick = { onToggleCompletion(task) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.RadioButtonUnchecked
                    },
                    contentDescription = if (task.isCompleted) {
                        "Desmarcar tarea completada"
                    } else {
                        "Marcar tarea como completada"
                    },
                    tint = if (task.isCompleted) {
                        EmeraldPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Contenido textual de la tarea (titulo, descripcion, estado y fecha)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    StatusBadge(isCompleted = task.isCompleted)
                }

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTimestamp(task.updatedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Acciones de edicion y eliminacion
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEditClick(task) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar tarea",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteClick(task) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Eliminar tarea",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Etiqueta compacta que indica si la tarea esta Completada o Pendiente.
 */
@Composable
private fun StatusBadge(isCompleted: Boolean) {
    val bgColor = if (isCompleted) CompletedBadgeBg else PendingBadgeBg
    val textColor = if (isCompleted) CompletedBadgeText else PendingBadgeText
    val text = if (isCompleted) "Completada" else "Pendiente"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Formatea una marca de tiempo en milisegundos a una cadena legible en espanol.
 */
private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX"))
    return formatter.format(Date(timestamp))
}
