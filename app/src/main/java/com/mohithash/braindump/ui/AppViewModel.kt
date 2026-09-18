package com.mohithash.braindump.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.braindump.App
import com.mohithash.braindump.ai.AiSettings
import com.mohithash.braindump.data.Task
import com.mohithash.braindump.domain.DayPlan
import com.mohithash.braindump.domain.DumpResult
import com.mohithash.braindump.domain.ParsedTask
import com.mohithash.braindump.domain.Review
import com.mohithash.braindump.domain.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    val plan: StateFlow<DayPlan> = app.store.flow("plan", DayPlan.serializer(), DayPlan())
    val review: StateFlow<Review> = app.store.flow("review", Review.serializer(), Review())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val tasks = db.tasks().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dump = MutableStateFlow<Job<DumpResult>>(Job.Idle)
    val dump: StateFlow<Job<DumpResult>> = _dump
    private val _planJob = MutableStateFlow<Job<Unit>>(Job.Idle)
    val planJob: StateFlow<Job<Unit>> = _planJob
    private val _reviewJob = MutableStateFlow<Job<Unit>>(Job.Idle)
    val reviewJob: StateFlow<Job<Unit>> = _reviewJob

    fun processDump(text: String, image: String?) {
        _dump.value = Job.Loading
        viewModelScope.launch {
            val projects = tasks.value.map { it.project }.filter { it.isNotBlank() }.distinct()
            _dump.value = runCatching { app.planner.dump(ai.value, settings.value, text, image, projects) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun acceptDump(items: List<ParsedTask>) = viewModelScope.launch {
        db.tasks().insert(items.map { Task(title = it.title, notes = it.notes, priority = it.priority.coerceIn(1, 3), due = it.due, project = it.project, estimateMin = it.estimate_min) })
        _dump.value = Job.Idle
    }
    fun clearDump() { _dump.value = Job.Idle }

    fun addQuick(title: String) = viewModelScope.launch { if (title.isNotBlank()) db.tasks().insertOne(Task(title = title.trim())) }
    fun toggle(t: Task) = viewModelScope.launch { db.tasks().update(t.copy(done = !t.done, doneAt = if (!t.done) System.currentTimeMillis() else 0)) }
    fun setPriority(t: Task, p: Int) = viewModelScope.launch { db.tasks().update(t.copy(priority = p)) }
    fun setDue(t: Task, due: String) = viewModelScope.launch { db.tasks().update(t.copy(due = due)) }
    fun delete(id: Long) = viewModelScope.launch { db.tasks().delete(id) }
    fun clearDone() = viewModelScope.launch { db.tasks().clearDone() }

    fun planToday(note: String) {
        _planJob.value = Job.Loading
        viewModelScope.launch {
            val open = db.tasks().open()
            _planJob.value = if (open.isEmpty()) Job.Failed("No open tasks to plan.")
            else runCatching { app.planner.planDay(ai.value, settings.value, open, note) }.fold({ app.store.set("plan", DayPlan.serializer(), it); Job.Done(Unit) }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun weeklyReview() {
        _reviewJob.value = Job.Loading
        viewModelScope.launch {
            val since = System.currentTimeMillis() - 7L * 86_400_000
            val all = db.tasks().since(since)
            _reviewJob.value = runCatching { app.planner.review(ai.value, settings.value, all.filter { it.done }, db.tasks().open()) }.fold({ app.store.set("review", Review.serializer(), it); Job.Done(Unit) }, { Job.Failed(it.message ?: "Failed") })
        }
    }

    fun isOverdue(t: Task) = !t.done && t.due.isNotBlank() && runCatching { LocalDate.parse(t.due) < LocalDate.now() }.getOrDefault(false)
    fun isToday(t: Task) = t.due == LocalDate.now().toString()
}
