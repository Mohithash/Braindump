package com.mohithash.braindump.ai

import com.mohithash.braindump.data.Task
import com.mohithash.braindump.domain.DayPlan
import com.mohithash.braindump.domain.DumpResult
import com.mohithash.braindump.domain.Review
import com.mohithash.braindump.domain.Settings
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlannerAi(private val client: AiClient) {
    private val dumpSchema = Schema.obj(
        "tasks" to Schema.arr(Schema.obj("title" to Schema.str, "priority" to Schema.int, "due" to Schema.str, "project" to Schema.str, "estimate_min" to Schema.int, "notes" to Schema.str)),
        "notes" to Schema.arr(Schema.str), "questions" to Schema.arr(Schema.str),
    )
    private val planSchema = Schema.obj(
        "summary" to Schema.str,
        "blocks" to Schema.arr(Schema.obj("start" to Schema.str, "end" to Schema.str, "title" to Schema.str, "task_ids" to Schema.arr(Schema.int), "kind" to Schema.enum("task", "break", "buffer"))),
        "deferred" to Schema.arr(Schema.str),
    )
    private val reviewSchema = Schema.obj("headline" to Schema.str, "done_well" to Schema.arr(Schema.str), "slipped" to Schema.arr(Schema.str), "suggestion" to Schema.str)

    private fun today() = "Today is ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE yyyy-MM-dd"))}."

    suspend fun dump(ai: AiSettings, s: Settings, text: String, image: String?, projects: List<String>): DumpResult {
        val system = """You turn a messy brain dump into a clean task list. ${today()}
            |tasks: one per concrete action, imperative title under 10 words. priority 1 = urgent/important, 2 = normal, 3 = someday. due: resolve relative dates ("Friday", "next week", "tomorrow") to yyyy-MM-dd, else empty.
            |project: reuse one of the existing projects when it fits (${projects.joinToString().ifBlank { "none yet" }}), otherwise a short new name or empty. estimate_min: rough minutes (15-240). notes: only if there is extra detail.
            |notes: non-actionable thoughts/ideas worth keeping. questions: anything ambiguous you would need answered (max 3).""".trimMargin()
        return client.ask(ai, system, text.ifBlank { "Extract tasks from the attached photo (whiteboard / notebook)." }, dumpSchema, image, 5000)
    }

    suspend fun planDay(ai: AiSettings, s: Settings, tasks: List<Task>, note: String): DayPlan {
        val system = """You are a realistic day planner. ${today()} Working hours ${s.workStart}-${s.workEnd}.
            |Build a time-blocked schedule from the tasks (referenced by id). Overdue and priority-1 tasks first, then due-today, then others if time allows. Group tiny tasks into one block.
            |Insert a lunch break and short buffers. Do not overfill: leave 20% slack. deferred: titles of tasks that do not fit today, with one reason each. summary: one motivating sentence.""".trimMargin()
        val user = tasks.joinToString("\n") { "#${it.id} [p${it.priority}] ${it.title}${if (it.due.isNotBlank()) " (due ${it.due})" else ""}${if (it.estimateMin > 0) " ~${it.estimateMin}m" else ""}" } +
            (if (note.isNotBlank()) "\n\nConstraints today: $note" else "")
        val p: DayPlan = client.ask(ai, system, user, planSchema, maxTokens = 4000)
        return p.copy(date = LocalDate.now().toString())
    }

    suspend fun review(ai: AiSettings, s: Settings, done: List<Task>, open: List<Task>): Review {
        val system = "You are a kind, honest productivity coach reviewing the last 7 days. ${today()} headline: one sentence. done_well: 2-4 items. slipped: 1-3 overdue/stale items worth a decision. suggestion: one specific change for next week."
        val user = "Completed:\n" + done.joinToString("\n") { "- ${it.title} (${it.project})" }.ifBlank { "(nothing)" } + "\n\nStill open:\n" + open.joinToString("\n") { "- ${it.title} due ${it.due.ifBlank { "—" }} p${it.priority} created ${LocalDate.ofEpochDay(it.createdAt / 86_400_000)}" }
        val r: Review = client.ask(ai, system, user, reviewSchema, maxTokens = 2000)
        return r.copy(generatedAt = System.currentTimeMillis())
    }
}
