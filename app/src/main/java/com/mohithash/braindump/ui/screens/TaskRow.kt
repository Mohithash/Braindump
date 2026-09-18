@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.braindump.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import com.mohithash.braindump.data.Task
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.prettyDate

@Composable
fun TaskRow(vm: AppViewModel, t: Task) {
    val cs = MaterialTheme.colorScheme
    val overdue = vm.isOverdue(t)
    val pColor = when (t.priority) { 1 -> cs.error; 3 -> cs.onSurfaceVariant; else -> cs.primary }
    ListItem(
        leadingContent = { Checkbox(t.done, { vm.toggle(t) }) },
        headlineContent = { Text(t.title, textDecoration = if (t.done) TextDecoration.LineThrough else null, color = if (t.done) cs.onSurfaceVariant else cs.onSurface) },
        supportingContent = {
            val bits = listOfNotNull(
                t.due.takeIf { it.isNotBlank() }?.let { if (overdue) "overdue · ${it.prettyDate()}" else it.prettyDate() },
                t.project.takeIf { it.isNotBlank() }, t.estimateMin.takeIf { it > 0 }?.let { "~${it}m" }, t.notes.takeIf { it.isNotBlank() })
            if (bits.isNotEmpty()) Text(bits.joinToString(" · "), color = if (overdue) cs.error else cs.onSurfaceVariant)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(when (t.priority) { 1 -> "!!" ; 3 -> "·"; else -> "!" }, style = MaterialTheme.typography.titleMedium, color = pColor, modifier = Modifier.clip(MaterialTheme.shapes.small))
                IconButton({ vm.delete(t.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) }
            }
        },
        colors = ListItemDefaults.colors(containerColor = if (t.done) cs.surfaceContainerLowest else if (overdue) cs.errorContainer.copy(alpha = 0.35f) else cs.surfaceContainerLow),
        modifier = Modifier.clip(MaterialTheme.shapes.large),
    )
}
