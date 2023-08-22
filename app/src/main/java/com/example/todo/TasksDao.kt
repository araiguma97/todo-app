package com.example.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.todo.Task

@Dao
interface TasksDao {
    @Query("SELECT * FROM tasks")
    fun getAll(): MutableList<Task>

    @Upsert
    fun upsertTask(task: Task)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE taskId = :taskId")
    fun updateCompleted(taskId: Int, isCompleted: Boolean)

    @Delete
    fun deleteTask(task: Task)
}