package com.example.planad.screens.main

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.planad.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User


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
    var expandedTaskId by remember { mutableStateOf<String?>(null) }

    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showDeleteProjectDialog by remember { mutableStateOf(false) }

    var showEditTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }

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
        loading = true
        errorMessage = ""
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
                            onClick = {
                                showEditProjectDialog = true
                                dropdownMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить проект") },
                            onClick = {
                                showDeleteProjectDialog = true
                                dropdownMenu = false
                            }
                        )
                    }
                }
            }
        )

        if (showEditProjectDialog) {
            EditProjectDialog(
                currentProjectName = projectName,
                onDismissRequest = { showEditProjectDialog = false },
                onSave = { newName ->
                    updateProjectName(
                        projectId = projectId,
                        newName = newName,
                        onSuccess = {
                            projectName = newName
                            showEditProjectDialog = false
                        },
                        onFailure = { e ->
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    )
                }
            )
        }

        if (showDeleteProjectDialog) {
            DeleteProjectDialog(
                projectId = projectId,
                onDismissRequest = { showDeleteProjectDialog = false },
                onBackTap = onBackTap
            )
        }

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
                            }
                            Box {
                                IconButton(
                                    onClick = {
                                        selectedTaskForEdit = task
                                    }
                                ) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = "Опции")
                                }
                                DropdownMenu(
                                    expanded = selectedTaskForEdit?.id == task.id,
                                    onDismissRequest = {
                                        selectedTaskForEdit = null
                                        expandedTaskId = null
                                    }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Редактировать")
                                        },
                                        onClick = {
                                            showEditTaskDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text("Удалить")
                                        },
                                        onClick = {
                                            deleteTask(
                                                projectId = projectId,
                                                taskId = task.id,
                                                onSuccess = {
                                                    tasks.value = tasks.value.filter { it.id != task.id }
                                                    selectedTaskForEdit = null
                                                    expandedTaskId = null
                                                },
                                                onFailure = { e ->
                                                    errorMessage = "Ошибка: ${e.message}"
                                                }
                                            )
                                            expandedTaskId = null
                                        }
                                    )
                                }
                            }
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

        if (showEditTaskDialog && selectedTaskForEdit != null) {
            EditTaskDialog(
                currentTask = selectedTaskForEdit!!,
                onDismissRequest = {
                    showEditTaskDialog = false
                    selectedTaskForEdit = null
                },
                onSave = { newTitle, newDescription ->
                    updateTask(
                        projectId = projectId,
                        taskId = selectedTaskForEdit!!.id,
                        title = newTitle,
                        description = newDescription,
                        onSuccess = {
                            tasks.value = tasks.value.map { task ->
                                if (task.id == selectedTaskForEdit!!.id) {
                                    task.copy(title = newTitle, description = newDescription)
                                } else {
                                    task
                                }
                            }
                            showEditTaskDialog = false
                            selectedTaskForEdit = null
                        },
                        onFailure = { e ->
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    )
                }
            )
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
                        onSuccess = { taskId ->
                            val taskWithId = newTask.copy(id = taskId)
                            tasks.value += taskWithId
                            showBottomSheet = false
                            taskName = ""
                            taskDescription = ""
                        },
                        onFailure = { e ->
                            errorMessage = "Ошибка: ${e.message}"
                            taskName = ""
                            taskDescription = ""
                            showBottomSheet = false
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

fun updateProjectName(
    projectId: String,
    newName: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId)
        .update("name", newName)
        .addOnSuccessListener {
            onSuccess()
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
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks")
        .add(task)
        .addOnSuccessListener { documentReference ->
            val taskId = documentReference.id
            onSuccess(taskId)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun deleteTask(
    projectId: String,
    taskId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks").document(taskId)
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun updateTask(
    projectId: String,
    taskId: String,
    title: String,
    description: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks").document(taskId)
        .update(
            mapOf(
                "title" to title,
                "description" to description
            )
        )
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
                val task = document.toObject(Task::class.java).copy(id = document.id)
                tasks.add(task)
                }
            onSuccess(tasks)
            }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

@Composable
fun EditTaskDialog(
    currentTask: Task,
    onDismissRequest: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var newTaskName by remember { mutableStateOf(currentTask.title) }
    var newTaskDescription by remember { mutableStateOf(currentTask.description) }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(280.dp)
            ) {
                Text(
                    text = "Редактировать задачу",
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = newTaskName,
                    onValueChange = { newTaskName = it },
                    label = { Text(text = "Название задачи") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newTaskDescription,
                    onValueChange = { newTaskDescription = it },
                    label = { Text("Описание задачи") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { onSave(newTaskName, newTaskDescription) },
                        enabled = newTaskName.isNotEmpty()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun EditProjectDialog(
    currentProjectName: String,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    var newProjectName by remember { mutableStateOf(currentProjectName) }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(280.dp)
            ) {
                Text(
                    text = "Редактировать название проекта",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    label = { Text("Новое название проекта") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = "Отмена"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { onSave(newProjectName) },
                            enabled = newProjectName.isNotEmpty()
                        ) {
                            Text(
                                text = "Сохранить"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteProjectDialog(
    onBackTap: () -> Unit,
    projectId: String,
    onDismissRequest: () -> Unit,
) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(280.dp)
            ) {
                Text(
                    text = "Вы уверены, что хотите удалить проект?",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = "Отмена"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
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
                        ) {
                            Text(
                                text = "Удалить"
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Task(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    var assignedUserId: String? = null
)
