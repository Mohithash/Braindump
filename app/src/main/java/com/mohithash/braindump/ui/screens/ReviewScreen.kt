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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.braindump.ui.AppViewModel
import com.mohithash.braindump.ui.BarChart
import com.mohithash.braindump.ui.EmptyState
import com.mohithash.braindump.ui.HeroCard
import com.mohithash.braindump.ui.Job
import com.mohithash.braindump.ui.Label
import com.mohithash.braindump.ui.ShapeIcon
import com.mohithash.braindump.ui.StatCard
import com.mohithash.braindump.ui.theme.Brand
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ReviewScreen(vm: AppViewModel) {
    val tasks by vm.tasks.collectAsState()
    val review by vm.review.collectAsState()
    val job by vm.reviewJob.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    val perDay = (0 until 7).map { LocalDate.now().minusDays(6L - it) }.map { d -> d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } to tasks.count { it.done && Instant.ofEpochMilli(it.doneAt).atZone(ZoneId.systemDefault()).toLocalDate() == d } }
    Scaffold(topBar = { TopAppBar(title = { Text("Weekly review") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard { Label("Completed · last 7 days"); Text("${perDay.sumOf { it.second }} tasks", style = MaterialTheme.typography.headlineSmall); BarChart(perDay, goal = 3, modifier = Modifier.padding(top = 8.dp), base = cs.primaryContainer, hit = cs.primary) }
            Button({ vm.weeklyReview() }, enabled = ai.configured && job != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (job == Job.Loading) { LoadingIndicator(Modifier.size(20.dp)); Spacer(Modifier.size(8.dp)); Text("Reviewing…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text(if (review.generatedAt == 0L) "Review my week" else "Review again") }
            }
            (job as? Job.Failed)?.let { StatCard(container = cs.errorContainer) { Text(it.message, color = cs.onErrorContainer) } }
            if (review.generatedAt == 0L) EmptyState(Icons.Default.Insights, "No review yet", "A kind, honest look at what got done, what slipped, and one change for next week.")
            else {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) { Label("This week", cs.onPrimary.copy(alpha = 0.8f)); Text(review.headline, style = MaterialTheme.typography.headlineSmall, color = cs.onPrimary) }
                Section(Icons.Default.EmojiEvents, "Went well", review.done_well, cs.secondaryContainer, cs.onSecondaryContainer)
                Section(Icons.Default.Warning, "Slipped — decide", review.slipped, cs.tertiaryContainer, cs.onTertiaryContainer)
                Section(Icons.Default.Science, "Try next week", listOf(review.suggestion), cs.primaryContainer, cs.onPrimaryContainer)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, items: List<String>, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    if (items.all { it.isBlank() }) return
    StatCard(container = bg) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { ShapeIcon(icon, fg.copy(alpha = 0.12f), fg, MaterialShapes.Sunny); Text(title, style = MaterialTheme.typography.titleMedium, color = fg) }
        items.filter { it.isNotBlank() }.forEach { Text("•  $it", style = MaterialTheme.typography.bodyLarge, color = fg, modifier = Modifier.padding(start = 4.dp, top = 2.dp)) }
    }
}
