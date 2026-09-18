package com.mohithash.braindump.domain

import kotlinx.serialization.Serializable

@Serializable
data class ParsedTask(val title: String, val priority: Int = 2, val due: String = "", val project: String = "", val estimate_min: Int = 0, val notes: String = "")
@Serializable data class DumpResult(val tasks: List<ParsedTask> = emptyList(), val notes: List<String> = emptyList(), val questions: List<String> = emptyList())

@Serializable data class Block(val start: String, val end: String, val title: String, val task_ids: List<Long> = emptyList(), val kind: String = "task")
@Serializable data class DayPlan(val summary: String = "", val blocks: List<Block> = emptyList(), val deferred: List<String> = emptyList(), val date: String = "")

@Serializable data class Review(val headline: String = "", val done_well: List<String> = emptyList(), val slipped: List<String> = emptyList(), val suggestion: String = "", val generatedAt: Long = 0)

@Serializable data class Settings(val name: String = "", val workStart: String = "09:00", val workEnd: String = "18:00", val onboarded: Boolean = false)
