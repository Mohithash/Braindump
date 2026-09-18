@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.braindump.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.braindump.domain.Settings
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.Label
import com.mohithash.braindump.ui.StatCard

@Composable
fun SettingsForm(initial: Settings, onChange: (Settings) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var start by remember { mutableStateOf(initial.workStart) }
    var end by remember { mutableStateOf(initial.workEnd) }
    fun emit() = onChange(initial.copy(name = name, workStart = start, workEnd = end))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, { name = it; emit() }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        Label("Working hours (for day plans)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(start, { start = it; emit() }, label = { Text("Start") }, placeholder = { Text("09:00") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
            OutlinedTextField(end, { end = it; emit() }, label = { Text("End") }, placeholder = { Text("18:00") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
        }
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Settings()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Get it out of your head") }, subtitle = { Text("Dump everything. Get a clean task list, a realistic day plan, and an honest weekly review.") }, scrollBehavior = scroll) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { SettingsForm(Settings()) { draft = it } }
            Button({ vm.saveSettings(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Start", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
