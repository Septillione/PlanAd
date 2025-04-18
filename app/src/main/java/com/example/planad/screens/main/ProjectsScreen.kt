package com.example.planad.screens.main

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planad.BottomBarScreen
import com.example.planad.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProjectsScreen(
    onProjectTap: (String) -> Unit
) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
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

        getProjects(
            onSuccess = {
                projects = it
                loading = false
            },
            onFailure = { e ->
                errorMessage = "Ошибка: ${e.message}"
                loading = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Проекты",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 100.dp)
        )

        if (loading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red
            )
        } else {
            if (userRole == "Руководитель") {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 160.dp, bottom = 230.dp)
                ) {
                    items(projects) { project ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    ambientColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.8f) else Color.Black.copy(
                                        alpha = 0.8f
                                    ),
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                                .clickable { onProjectTap(project.id) }
                        ) {
                            Row(
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_folder_24),
                                    contentDescription = "Проект:",
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = project.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 160.dp, bottom = 180.dp)
                ) {
                    items(projects) { project ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    ambientColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.8f) else Color.Black.copy(
                                        alpha = 0.8f
                                    ),
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                                .clickable { onProjectTap(project.id) }
                        ) {
                            Row(
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_folder_24),
                                    contentDescription = "Проект:",
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = project.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

        }

        if (userRole == "Руководитель") {
            FilledTonalButton(
                onClick = {showBottomSheet = true},
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorResource(id = R.color.lightBlue).copy(alpha = 0.3f),
                    contentColor = colorResource(id = R.color.blue)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 150.dp)
            ) {
                Text(
                    text = "Добавить проект",
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
            BottomSheetProject(
                onDismissRequest = {showBottomSheet = false},
                onProjectAdded = { name ->
                    val newProject = Project(name = name)
                    addProject(
                        newProject,
                        onSuccess = {
                            projects = projects + newProject
                            showBottomSheet = false
                            projectName = ""
                        },
                        onFailure = { e ->
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    )
                },
                projectName = projectName,
                onProjectNameChange = { projectName = it }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetProject(
    onDismissRequest: () -> Unit,
    onProjectAdded: (String) -> Unit,
    projectName: String,
    onProjectNameChange: (String) -> Unit
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
                text = "Добавить проект",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = projectName,
                onValueChange = onProjectNameChange,
                label = {Text("Название проекта")},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (projectName.isNotEmpty()) {
                        onProjectAdded(projectName)
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
                    text = "Создать проект",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

fun addProject(
    project: Project,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects")
        .add(project)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun deleteProject(
    projectId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("projects").document(projectId)
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

fun getProjects(
    onSuccess: (List<Project>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("projects")
        .get()
        .addOnSuccessListener { documents ->
            val projectList = documents.map { document ->
                Project(
                    id = document.id,
                    name = document.getString("name") ?: "Unnamed"
                )
            }
            onSuccess(projectList)
        }
        .addOnFailureListener {e ->
            onFailure(e)
        }
}

fun getUserRole(
    userId: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val role = document.getString("role")?:"Сотрудник"
                onSuccess(role)
            } else {
                onFailure(Exception("Пользователь не найден"))
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

data class Project(
    val id: String = "",
    val name: String = ""
)


