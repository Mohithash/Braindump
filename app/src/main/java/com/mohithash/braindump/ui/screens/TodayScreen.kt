@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.braindump.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.EmptyState
import com.mohithash.braindump.ui.HeroCard
import com.mohithash.braindump.ui.Job
import com.mohithash.braindump.ui.Label
import com.mohithash.braindump.ui.StatCard
import com.mohithash.braindump.ui.theme.Brand
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(vm: AppViewModel, onDump: () -> Unit, onSettings: () -> Unit) {
    val tasks by vm.tasks.collectAsState()
    val plan by vm.plan.collectAsState()
    val job by vm.planJob.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    var note by remember { mutableStateOf("") }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val focus = tasks.filter { !it.done && (vm.isOverdue(it) || vm.isToday(it) || it.priority == 1) }
    val doneToday = tasks.count { it.done && it.doneAt >= LocalDate.now().toEpochDay() * 86_400_000 }
    val planIsToday = plan.date == LocalDate.now().toString()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { MediumFlexibleTopAppBar(title = { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM"))) }, subtitle = { Text("${focus.size} to focus on · $doneToday done today") }, actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }, scrollBehavior = scroll, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface)) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onDump, icon = { Icon(Icons.Default.Psychology, null) }, text = { Text("Brain dump") }, containerColor = cs.primary, contentColor = cs.onPrimary) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Sunny) {
                    val on = cs.onPrimary
                    Label("Plan my day", on.copy(alpha = 0.8f))
                    Text(if (planIsToday) plan.summary else "Turn today's tasks into a time‑blocked schedule.", style = MaterialTheme.typography.titleLarge, color = on)
                    if (!planIsToday) OutlinedTextField(note, { note = it }, placeholder = { Text("Constraints? e.g. meetings 2–4pm, leave by 5", color = on.copy(alpha = 0.6f)) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), shape = MaterialTheme.shapes.large,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = on, unfocusedTextColor = on, focusedBorderColor = on, unfocusedBorderColor = on.copy(alpha = 0.5f), cursorColor = on))
                    Button({ vm.planToday(note) }, enabled = ai.configured && job != Job.Loading, shapes = ButtonDefaults.shapes(), colors = ButtonDefaults.buttonColors(containerColor = cs.secondary, contentColor = cs.onSecondary), modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 4.dp)) {
                        if (job == Job.Loading) { LoadingIndicator(Modifier.size(18.dp)); Spacer(Modifier.size(8.dp)); Text("Scheduling…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text(if (planIsToday) "Re‑plan" else "Plan today") }
                    }
                    (job as? Job.Failed)?.let { Text(it.message, color = cs.errorContainer, style = MaterialTheme.typography.bodySmall) }
                }
            }
            if (planIsToday) {
                item { Text("Schedule", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp)) }
                items(plan.blocks) { b ->
                    val isBreak = b.kind != "task"
                    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(if (isBreak) cs.surfaceContainerLowest else cs.surfaceContainerLow).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(56.dp)) { Text(b.start, style = MaterialTheme.typography.labelLarge, color = cs.primary); Text(b.end, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant) }
                        Box(Modifier.width(4.dp).height(36.dp).background(if (isBreak) cs.outlineVariant else cs.primary, MaterialTheme.shapes.small))
                        Column(Modifier.weight(1f)) {
                            Text(b.title, style = MaterialTheme.typography.bodyLarge, color = if (isBreak) cs.onSurfaceVariant else cs.onSurface)
                            val linked = tasks.filter { it.id in b.task_ids }
                            if (linked.isNotEmpty()) { val d = linked.count { it.done }; LinearWavyProgressIndicator(progress = { d.toFloat() / linked.size }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
                        }
                    }
                }
                if (plan.deferred.isNotEmpty()) item { StatCard(container = cs.tertiaryContainer) { Label("Not today", cs.onTertiaryContainer); plan.deferred.forEach { Text("•  $it", color = cs.onTertiaryContainer) } } }
            }
            item { Text("Focus", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp)) }
            if (focus.isEmpty()) item { EmptyState(Icons.Default.WbSunny, "Clear horizon", "Nothing overdue, due today or urgent. Dump what's on your mind or pick from Tasks.") }
            items(focus, key = { it.id }) { TaskRow(vm, it) }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
