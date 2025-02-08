package com.example.planad.screens.main

import android.app.ActivityManager.TaskDescription
import android.icu.number.NumberFormatter.UnitWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import com.example.planad.R
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBackTap: () -> Unit,
    onExecutorSelect: () -> Unit,
    projectId: String
) {
    var tasks = remember { mutableStateOf<List<Task>>(emptyList()) }
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var projectName by remember { mutableStateOf("Задачи проекта") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var dropdownMenu by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("Сотрудник") }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            getUserRole(
                userId = userId,
                onSuccess = { role ->
                    userRole = role
                },
                onFailure = { e ->
                    errorMessage = "Ошибка: ${e.message}"
                }
            )
        } else {
            errorMessage = "Пользователь не авторизован"
        }
    }

    // Получаем название проекта
    LaunchedEffect(projectId) {
        getProjectName(
            projectId = projectId,
            onSuccess = { name ->
                projectName = name // Обновляем название проекта
            },
            onFailure = { e ->
                errorMessage = "Ошибка: ${e.message}"
            }
        )

        // Получаем задачи проекта
        getTasksForProject(
            projectId = projectId,
            onSuccess = {
                tasks.value = it
                loading = false
            },
            onFailure = { e ->
                errorMessage = "Ошибка: ${e.message}"
                loading = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = projectName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                ) }, // Используем название проекта
            navigationIcon = {
                IconButton(onClick = onBackTap) {
                    Icon(Icons.Outlined.Home, contentDescription = "Назад", modifier = Modifier.size(30.dp))
                }
            },
            actions = {
                if (userRole == "Руководитель") {
                    IconButton(
                        onClick = { dropdownMenu = !dropdownMenu }
                    ) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Опции", modifier = Modifier.size(30.dp))
                    }
                    DropdownMenu(
                        expanded = dropdownMenu,
                        onDismissRequest = { dropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать проект") },
                            onClick = { }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить проект") },
                            onClick = {
                                deleteProject(
                                    projectId = projectId,
                                    onSuccess = {
                                        getProjects(
                                            onSuccess = { updatedProjects ->
                                                projects = updatedProjects
                                            },
                                            onFailure = { e ->
                                                errorMessage = "Ошибка при получении обновленных проектов: ${e.message}"
                                            }
                                        )
                                        onBackTap()
                                    },
                                    onFailure = { e ->
                                        errorMessage = "Ошибка при удалении проекта: ${e.message}"
                                    }
                                    )
                            }
                        )
                    }
                }
            }
        )

        if (loading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        } else {
            if (userRole == "Руководитель") {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 120.dp, bottom = 180.dp)
                ) {
                    items(tasks.value) { task ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {

                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = "Опции", modifier = Modifier.size(30.dp))
                                }
                            }

                            Text(
                                text = task.description,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 120.dp, bottom = 120.dp)
                ) {
                    items(tasks.value) { task ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .padding(16.dp)
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
                        }
                    }
                }
            }
        }

        if (userRole == "Руководитель") {
            FilledTonalButton(
                onClick = { showBottomSheet = true },
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorResource(id = R.color.lightBlue).copy(alpha = 0.3f),
                    contentColor = colorResource(id = R.color.blue)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 90.dp)
            ) {
                Text(
                    text = "Добавить задачу",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "Add Icon",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        if (showBottomSheet) {
            BottomSheetTask(
                onDismissRequest = { showBottomSheet = false },
                onTaskAdded = { name, description ->
                    val newTask = Task(title = name, description = description)
                    addTask(
                        projectId = projectId,
                        task = newTask,
                        onSuccess = {
                            tasks.value += newTask
                            showBottomSheet = false
                            taskName = ""
                            taskDescription = ""
                        },
                        onFailure = { e ->
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    )
                },
                taskName = taskName,
                onTaskNameChange = { taskName = it },
                taskDescription = taskDescription,
                onTaskDescription = { taskDescription = it }
            )
        }
    }
}


fun getProjectName(
    projectId: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val projectName = document.getString("name") ?: "Проект без названия"
                onSuccess(projectName)
            } else {
                onFailure(Exception("Проект не найден"))
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTask(
    onDismissRequest: () -> Unit,
    onTaskAdded: (String, String) -> Unit,
    taskName: String,
    onTaskNameChange: (String) -> Unit,
    taskDescription: String,
    onTaskDescription: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Добавить задачу",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = taskName,
                onValueChange = onTaskNameChange,
                label = {Text("Название задачи")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = taskDescription,
                onValueChange = onTaskDescription,
                label = {Text("Описание задачи")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (taskName.isNotEmpty()) {
                        onTaskAdded(taskName, taskDescription)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(250.dp, 50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Создать задачу",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

fun addTask(
    projectId: String,
    task: Task,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks")
        .add(task)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun deleteTask(
    taskId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("tasks").document(taskId)
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun getTasksForProject(
    projectId: String,
    onSuccess: (List<Task>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks")
        .get()
        .addOnSuccessListener { result ->
            val tasks = mutableListOf<Task>()
            for (document in result) {
                val task = document.toObject(Task::class.java)
                tasks.add(task)
                }
            onSuccess(tasks)
            }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

data class Task(
    var id: String = "",
    val title: String = "",
    val description: String = ""
)
