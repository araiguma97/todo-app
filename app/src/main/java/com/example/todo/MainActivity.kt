package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.ui.theme.TodoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    var dao = RoomApplication.database.tasksDao()
    var tasks = mutableStateListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoTheme {
                MainScreen(tasks)
            }
        }
        loadTasks()
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.getAll().forEach { todo ->
                    tasks.add(todo)
                }
            }
        }
    }

    private fun postTask(title: String) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.upsertTask(Task(title = title))

                tasks.clear()
                loadTasks()
            }
        }
    }

    private fun completeTask(taskId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.updateCompleted(taskId, true)

                tasks.clear()
                loadTasks()
            }
        }
    }

    private fun activateTask(taskId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.updateCompleted(taskId, false)

                tasks.clear()
                loadTasks()
            }
        }
    }

    private fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.deleteTask(task)

                tasks.clear()
                loadTasks()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(tasks: SnapshotStateList<Task>) {
        var text: String by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("To Do") }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    if (text.isEmpty()) return@FloatingActionButton
                    postTask(text)
                    text = ""
                }) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }
        ) { padding ->
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(padding).weight(1f)
                ) {
                    items(tasks) { task ->
                        key(task.taskId) {
                            TaskItem(task)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { it -> text = it },
                        modifier = Modifier.weight(6f)
                    )
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (text.isEmpty()) return@IconButton
                            postTask(text)
                            text = ""
                        },
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "AddCircle")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TaskItem(task: Task) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                modifier = Modifier.fillMaxWidth().weight(1f),
                onCheckedChange = {
                    if (it) {
                        completeTask(task.taskId)
                    } else {
                        activateTask(task.taskId)
                    }
                }
            )
            Text(
                text = task.title,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().weight(5f),
                textDecoration = if (task.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    null
                }
            )
        }
    }
}