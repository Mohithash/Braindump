@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.braindump.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.EmptyState

@Composable
fun TasksScreen(vm: AppViewModel) {
    val tasks by vm.tasks.collectAsState()
    val cs = MaterialTheme.colorScheme
    var quick by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("All") }
    val projects = listOf("All") + tasks.map { it.project.ifBlank { "Inbox" } }.distinct().sorted()
    val shown = tasks.filter { project == "All" || it.project.ifBlank { "Inbox" } == project }
    val done = shown.count { it.done }
    Scaffold(topBar = { TopAppBar(title = { Text("All tasks") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { if (done > 0) TextButton(vm::clearDone) { Text("Clear $done done") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(quick, { quick = it }, placeholder = { Text("Quick add a task") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraLarge)
                    Button({ vm.addQuick(quick); quick = "" }, enabled = quick.isNotBlank(), shapes = ButtonDefaults.shapes()) { Text("Add") }
                }
            }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { projects.forEach { p -> FilterChip(selected = project == p, onClick = { project = p }, label = { Text(p) }) } } }
            if (shown.isEmpty()) item { EmptyState(Icons.Default.Checklist, "No tasks", "Add one above or do a brain dump.") }
            items(shown, key = { it.id }) { TaskRow(vm, it) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
