@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.braindump.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.Job
import com.mohithash.braindump.ui.Label
import com.mohithash.braindump.ui.MealPhoto
import com.mohithash.braindump.ui.Photo
import com.mohithash.braindump.ui.StatCard
import com.mohithash.braindump.ui.prettyDate

@Composable
fun DumpScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val job by vm.dump.collectAsState()
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b -> b?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> u?.let { photo = Photo.fromUri(ctx, it) } }
    LaunchedEffect(job) { (job as? Job.Done)?.let { selected = it.value.tasks.indices.toSet() } }

    Scaffold(topBar = { TopAppBar(title = { Text("Brain dump") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val j = job) {
                is Job.Done -> {
                    StatCard(container = cs.primaryContainer) { Label("Found ${j.value.tasks.size} tasks", cs.onPrimaryContainer); Text("Untick anything you don't want.", color = cs.onPrimaryContainer, style = MaterialTheme.typography.bodySmall) }
                    j.value.tasks.forEachIndexed { i, t ->
                        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(i in selected, { selected = if (i in selected) selected - i else selected + i })
                            Column(Modifier.weight(1f)) {
                                Text(t.title, style = MaterialTheme.typography.bodyLarge)
                                Text(listOfNotNull(listOf("!! urgent", "! normal", "· someday")[t.priority.coerceIn(1, 3) - 1], t.due.takeIf { it.isNotBlank() }?.prettyDate(), t.project.takeIf { it.isNotBlank() }, t.estimate_min.takeIf { it > 0 }?.let { "~${it}m" }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                    if (j.value.notes.isNotEmpty()) StatCard(container = cs.secondaryContainer) { Label("Kept as notes", cs.onSecondaryContainer); j.value.notes.forEach { Text("•  $it", color = cs.onSecondaryContainer) } }
                    if (j.value.questions.isNotEmpty()) StatCard(container = cs.tertiaryContainer) { Label("Worth clarifying", cs.onTertiaryContainer); j.value.questions.forEach { Text("?  $it", color = cs.onTertiaryContainer) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton({ vm.clearDump() }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp)) { Text("Discard") }
                        Button({ vm.acceptDump(j.value.tasks.filterIndexed { i, _ -> i in selected }); text = ""; photo = null }, enabled = selected.isNotEmpty(), shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(2f).height(52.dp)) { Text("Add ${selected.size} tasks") }
                    }
                }
                else -> {
                    StatCard {
                        Label("Everything on your mind")
                        photo?.let { p -> Box(Modifier.fillMaxWidth()) { Image(p.bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large)); FilledTonalIconButton({ photo = null }, Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Close, null) } } }
                        OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 8, shape = MaterialTheme.shapes.large,
                            placeholder = { Text("call dentist tomorrow, finish the Q3 deck by Friday, buy a gift for Sam, maybe start learning piano, ask Priya about the invoice…") })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Whiteboard") }
                            OutlinedButton({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery") }
                        }
                    }
                    (j as? Job.Failed)?.let { StatCard(container = cs.errorContainer) { Text(it.message, color = cs.onErrorContainer) } }
                    if (!ai.configured) Text("Add an API key in Settings to sort your dump into tasks.", color = cs.error, style = MaterialTheme.typography.bodySmall)
                    Button({ vm.processDump(text, photo?.base64) }, enabled = ai.configured && (text.isNotBlank() || photo != null) && job != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        if (job == Job.Loading) { LoadingIndicator(Modifier.size(22.dp)); Spacer(Modifier.size(10.dp)); Text("Sorting it out…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Sort into tasks", style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
