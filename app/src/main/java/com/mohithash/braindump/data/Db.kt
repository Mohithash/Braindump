package com.mohithash.braindump.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    /** 1 = high, 2 = normal, 3 = low */
    val priority: Int = 2,
    /** yyyy-MM-dd or empty */
    val due: String = "",
    val project: String = "",
    val estimateMin: Int = 0,
    val done: Boolean = false,
    val doneAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done, CASE WHEN due = '' THEN 1 ELSE 0 END, due, priority, createdAt") fun all(): Flow<List<Task>>
    @Query("SELECT * FROM tasks WHERE done = 0") suspend fun open(): List<Task>
    @Query("SELECT * FROM tasks WHERE doneAt >= :since OR (done = 0 AND createdAt >= :since)") suspend fun since(since: Long): List<Task>
    @Insert suspend fun insert(t: List<Task>)
    @Insert suspend fun insertOne(t: Task): Long
    @Update suspend fun update(t: Task)
    @Query("DELETE FROM tasks WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM tasks WHERE done = 1") suspend fun clearDone()
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun tasks(): TaskDao }
