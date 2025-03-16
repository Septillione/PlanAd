package com.example.planad.screens.main

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserTasksScreen(
    onBackTap: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    if (currentUserId == null) {
        errorMessage = "Пользователь не авторизован"
        return
    }

    // Загрузка пользователей
    LaunchedEffect(Unit) {
        getUsers(
            onSuccess = { userList ->
                users = userList
            },
            onFailure = { e ->
                errorMessage = "Ошибка при загрузке пользователей: ${e.message}"
            }
        )
    }

    // Загрузка задач из всех проектов
    LaunchedEffect(Unit) {
        loading = true
        errorMessage = ""
        getAllTasks(
            onSuccess = { taskList ->
                tasks = taskList
                Log.d("UserTasksScreen", "Все задачи: $taskList")
                loading = false
            },
            onFailure = { e ->
                errorMessage = "Ошибка: ${e.message}"
                loading = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Мои задачи",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 100.dp)
        )

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Фильтруем задачи для текущего пользователя
            val myTasks = tasks.filter { currentUserId in it.assignedUserIds }

            if (myTasks.isEmpty()) {
                Text(
                    text = "У вас нет задач",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 140.dp, bottom = 140.dp)
                ) {
                    items(myTasks) { task ->
                        val assignedEmployees = users.filter { it.id in task.assignedUserIds && it.role == "Сотрудник" }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = task.description,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Text(
                                    text = "Статус: ${task.status}",
                                    fontSize = 14.sp,
                                    color = when (task.status) {
                                        "Выполняется" -> Color.Blue
                                        "Завершена" -> Color.Green
                                        "Отменена" -> Color.Red
                                        else -> Color.Gray
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (assignedEmployees.isNotEmpty()) {
                                    Text(
                                        text = "Исполнители: ${assignedEmployees.joinToString { "${it.firstName} ${it.lastName}" }}",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getAllTasks(
    onSuccess: (List<Task>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val allTasks = mutableListOf<Task>()

    db.collection("projects")
        .get()
        .addOnSuccessListener { projects ->
            for (project in projects) {
                val projectId = project.id
                db.collection("projects")
                    .document(projectId)
                    .collection("tasks")
                    .get()
                    .addOnSuccessListener { tasks ->
                        for (task in tasks) {
                            val taskData = Task(
                                id = task.id,
                                title = task.getString("title") ?: "",
                                description = task.getString("description") ?: "",
                                status = task.getString("status") ?: "Выполняется",
                                assignedUserIds = task.get("assignedUserIds") as? List<String> ?: emptyList()
                            )
                            allTasks.add(taskData)
                        }
                        onSuccess(allTasks)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}


