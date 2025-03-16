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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material3.OutlinedButton
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
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var projectName by remember { mutableStateOf("Задачи проекта") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var dropdownMenu by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("Сотрудник") }

    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showDeleteProjectDialog by remember { mutableStateOf(false) }

    var showEditTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }

    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // Получаем список пользователей
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
        getUsers(
            onSuccess = { userList ->
                users = userList
            },
            onFailure = { e ->
                errorMessage = "Ошибка при загрузке пользователей: ${e.message}"
            }
        )
    }

    // Получаем задачи проекта
    LaunchedEffect(projectId) {
        loading = true
        errorMessage = ""
        getProjectName(
            projectId = projectId,
            onSuccess = { name ->
                projectName = name
            },
            onFailure = { e ->
                errorMessage = "Ошибка: ${e.message}"
            }
        )

        getTasksForProject(
            projectId = projectId,
            onSuccess = {
                tasks = it
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
                )
            },
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp, bottom = 180.dp)
            ) {
                items(tasks) { task ->

                    val assignedEmployees = users.filter { it.id in task.assignedUserIds && it.role == "Сотрудник" }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Основная информация о задаче
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Название задачи
                            Text(
                                text = task.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Описание задачи
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
                        // Кнопки редактирования и удаления
                        if (userRole == "Руководитель") {
                            IconButton(
                                onClick = {
                                    selectedTaskForEdit = task
                                    showEditTaskDialog = true
                                }
                            ) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "Редактировать")
                            }

                            IconButton(
                                onClick = {
                                    taskToDelete = task
                                    showDeleteTaskDialog = true
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }

            if (showEditTaskDialog && selectedTaskForEdit != null) {
                EditTaskDialog(
                    currentTask = selectedTaskForEdit!!,
                    users = users,
                    onDismissRequest = {
                        showEditTaskDialog = false
                        selectedTaskForEdit = null
                    },
                    onSave = { newTitle, newDescription, newStatus, newExecutorIds ->
                        updateTask(
                            projectId = projectId,
                            taskId = selectedTaskForEdit!!.id,
                            title = newTitle,
                            description = newDescription,
                            status = newStatus,
                            assignedUserIds = newExecutorIds,
                            onSuccess = {
                                tasks = tasks.map { task ->
                                    if (task.id == selectedTaskForEdit!!.id) {
                                        task.copy(
                                            title = newTitle,
                                            description = newDescription,
                                            status = newStatus,
                                            assignedUserIds = newExecutorIds
                                        )
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

            if (showDeleteTaskDialog && taskToDelete != null) {
                DeleteTaskDialog(
                    task = taskToDelete!!,
                    onDismissRequest = {
                        showDeleteTaskDialog = false
                        taskToDelete = null
                    },
                    onDeleteConfirmed = {
                        deleteTask(
                            projectId = projectId,
                            taskId = taskToDelete!!.id,
                            onSuccess = {
                                tasks = tasks.filter { it.id != taskToDelete!!.id }
                                showDeleteTaskDialog = false
                                taskToDelete = null
                            },
                            onFailure = { e ->
                                errorMessage = "Ошибка: ${e.message}"
                                showDeleteTaskDialog = false
                                taskToDelete = null
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
                    onTaskAdded = { name, description, executorIds ->
                        val newTask = Task(
                            title = name,
                            description = description,
                            assignedUserIds = executorIds
                        )
                        addTask(
                            projectId = projectId,
                            task = newTask,
                            onSuccess = { taskId ->
                                val taskWithId = newTask.copy(id = taskId)
                                tasks += taskWithId
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
                    onTaskDescription = { taskDescription = it },
                    users = users,
                    onExecutorSelect = { executorIds ->
                        // Обработка выбора исполнителей
                    }
                )
            }
        }
    }
}

fun getUsers(
    onSuccess: (List<AppUser>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .get()
        .addOnSuccessListener { result ->
            val users = mutableListOf<AppUser>()
            for (document in result) {
                val user = AppUser(
                    id = document.id,
                    firstName = document.getString("firstName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    role = document.getString("role") ?: "",
                )
                users.add(user)
            }
            onSuccess(users)
        }
        .addOnFailureListener { e ->
            onFailure(e)
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

fun filterEmployees(users: List<AppUser>): List<AppUser> {
    return users.filter { it.role == "Сотрудник" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetTask(
    onDismissRequest: () -> Unit,
    onTaskAdded: (String, String, List<String>) -> Unit,
    taskName: String,
    onTaskNameChange: (String) -> Unit,
    taskDescription: String,
    onTaskDescription: (String) -> Unit,
    users: List<AppUser>, // Все пользователи
    onExecutorSelect: (List<String>) -> Unit
) {
    var selectedExecutorIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showExecutorDropdown by remember { mutableStateOf(false) }

    // Фильтруем пользователей, оставляя только сотрудников
    val employees = filterEmployees(users)

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

            Spacer(modifier = Modifier.height(8.dp))

            // Выбор исполнителей
            Text(
                text = "Выберите исполнителей",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Список сотрудников с чекбоксами
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Ограничиваем высоту списка
            ) {
                items(employees) { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedExecutorIds.contains(user.id),
                            onCheckedChange = { isChecked ->
                                selectedExecutorIds = if (isChecked) {
                                    selectedExecutorIds + user.id
                                } else {
                                    selectedExecutorIds - user.id
                                }
                            }
                        )
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для создания задачи
            Button(
                onClick = {
                    if (taskName.isNotEmpty()) {
                        onTaskAdded(taskName, taskDescription, selectedExecutorIds)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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

    val taskData = hashMapOf(
        "title" to task.title,
        "description" to task.description,
        "status" to task.status,
        "assignedUserId" to task.assignedUserIds
    )

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
    status: String,
    assignedUserIds: List<String>,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects").document(projectId).collection("tasks").document(taskId)
        .update(
            mapOf(
                "title" to title,
                "description" to description,
                "status" to status,
                "assignedUserIds" to assignedUserIds,
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
                val task = Task(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description") ?: "",
                    status = document.getString("status") ?: "Выполняется",
                    assignedUserIds = document.get("assignedUserIds") as? List<String> ?: emptyList()
                )
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
    users: List<AppUser>, // Все пользователи
    onDismissRequest: () -> Unit,
    onSave: (String, String, String, List<String>) -> Unit
) {
    var newTaskName by remember { mutableStateOf(currentTask.title) }
    var newTaskDescription by remember { mutableStateOf(currentTask.description) }
    var selectedStatus by remember { mutableStateOf(currentTask.status) }
    var selectedExecutorIds by remember { mutableStateOf(currentTask.assignedUserIds) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    // Фильтруем пользователей, оставляя только сотрудников
    val employees = filterEmployees(users)
    val statusOptions = listOf("Выполняется", "Завершена", "Отменена")

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
                    label = { Text("Название задачи") },
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showStatusDropdown = true },
                ) {
                    Text(selectedStatus)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Выбрать статус"
                    )
                }

                DropdownMenu(
                    expanded = showStatusDropdown,
                    onDismissRequest = { showStatusDropdown = false }
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                selectedStatus = status
                                showStatusDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Выбор исполнителей
                Text(
                    text = "Исполнители",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    items(employees) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedExecutorIds.contains(user.id),
                                onCheckedChange = { isChecked ->
                                    selectedExecutorIds = if (isChecked) {
                                        selectedExecutorIds + user.id
                                    } else {
                                        selectedExecutorIds - user.id
                                    }
                                }
                            )
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

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
                        onClick = {
                            onSave(newTaskName, newTaskDescription, selectedStatus, selectedExecutorIds)
                        },
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

@Composable
fun DeleteTaskDialog(
    task: Task, // Задача, которую нужно удалить
    onDismissRequest: () -> Unit, // Закрытие диалога
    onDeleteConfirmed: () -> Unit // Подтверждение удаления
) {
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
                // Заголовок диалога
                Text(
                    text = "Удалить задачу?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Описание задачи
                Text(
                    text = "Вы уверены, что хотите удалить задачу \"${task.title}\"?",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Кнопки "Отмена" и "Удалить"
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
                        onClick = onDeleteConfirmed
                    ) {
                        Text(
                            text = "Удалить",
                            color = Color.Red
                        )
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
    var status: String = "Выполняется",
    var assignedUserIds: List<String> = emptyList()
)

data class AppUser(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "",
    val password: String = ""
)
