@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.braindump.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.braindump.ui.screens.DumpScreen
import com.mohithash.braindump.ui.screens.OnboardingScreen
import com.mohithash.braindump.ui.screens.ReviewScreen
import com.mohithash.braindump.ui.screens.SettingsScreen
import com.mohithash.braindump.ui.screens.TasksScreen
import com.mohithash.braindump.ui.screens.TodayScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    TODAY("today", "Today", Icons.Outlined.Today, Icons.Filled.Today),
    DUMP("dump", "Dump", Icons.Outlined.Psychology, Icons.Filled.Psychology),
    TASKS("tasks", "Tasks", Icons.Outlined.Checklist, Icons.Filled.Checklist),
    REVIEW("review", "Review", Icons.Outlined.Insights, Icons.Filled.Insights),
}

@Composable
fun Nav(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    if (!s.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val current = back?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }
    Scaffold(bottomBar = {
        if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Tab.entries.forEach { tab ->
                val sel = current?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(selected = sel, onClick = { nav.navigate(tab.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(if (sel) tab.selected else tab.icon, tab.label) }, label = { Text(tab.label) })
            }
        }
    }) { pad ->
        NavHost(nav, Tab.TODAY.route, Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.TODAY.route) { TodayScreen(vm, onDump = { nav.navigate(Tab.DUMP.route) }, onSettings = { nav.navigate("settings") }) }
            composable(Tab.DUMP.route) { DumpScreen(vm, onSettings = { nav.navigate("settings") }) }
            composable(Tab.TASKS.route) { TasksScreen(vm) }
            composable(Tab.REVIEW.route) { ReviewScreen(vm) }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
