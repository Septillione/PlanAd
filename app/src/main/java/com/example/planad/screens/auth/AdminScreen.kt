package com.example.planad.screens.auth

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planad.R
import com.example.planad.screens.main.AppUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog

@Composable
fun AdminScreen(
    onBack: () -> Unit
) {
    val auth = Firebase.auth

    var errorMessage by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    // Загрузка списка пользователей
    LaunchedEffect(Unit) {
        loadUsers(
            onSuccess = { userList ->
                users = userList
            },
            onFailure = { e ->
                errorMessage = "Ошибка при загрузке пользователей: ${e.message}"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Список пользователей",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 30.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список пользователей с весом (weight), чтобы занять всё доступное пространство
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn {
                items(users) { user ->
                    UserItem(
                        user = user,
                        onDelete = {
                            deleteUser(
                                userId = user.id,
                                onSuccess = {
                                    // Обновляем список пользователей после удаления
                                    loadUsers(
                                        onSuccess = { userList ->
                                            users = userList
                                        },
                                        onFailure = { e ->
                                            errorMessage = "Ошибка при загрузке пользователей: ${e.message}"
                                        }
                                    )
                                },
                                onFailure = { e ->
                                    errorMessage = "Ошибка при удалении пользователя: ${e.message}"
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // Кнопка "Зарегистрировать пользователя"
        Button(
            onClick = { showRegistrationDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.darkBlue),
                contentColor = colorResource(id = R.color.white)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Зарегистрировать пользователя",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }

    if (showRegistrationDialog) {
        RegistrationDialog(
            onDismiss = { showRegistrationDialog = false },
            onRegister = { email, password, firstName, lastName, role ->
                signUp(
                    auth = auth,
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    role = role,
                    onSuccess = {
                        // Обновляем список пользователей после успешной регистрации
                        loadUsers(
                            onSuccess = { userList ->
                                users = userList
                            },
                            onFailure = { e ->
                                errorMessage = "Ошибка при загрузке пользователей: ${e.message}"
                            }
                        )
                    },
                    onFailure = { e ->
                        errorMessage = "Ошибка при регистрации: ${e.message}"
                    }
                )
            }
        )
    }
}

@Composable
fun RegistrationDialog(
    onDismiss: () -> Unit,
    onRegister: (email: String, password: String, firstName: String, lastName: String, role: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectRole by remember { mutableStateOf("Сотрудник") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Регистрация нового пользователя",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Выберите роль",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalButton(
                        onClick = { selectRole = "Сотрудник" },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectRole == "Сотрудник") colorResource(id = R.color.lightBlue).copy(alpha = 0.3f) else Color.LightGray
                        ),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.size(150.dp, 60.dp)
                    ) {
                        Text("Сотрудник")
                    }
                    FilledTonalButton(
                        onClick = { selectRole = "Руководитель" },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectRole == "Руководитель") colorResource(id = R.color.lightBlue).copy(alpha = 0.3f) else Color.LightGray
                        ),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.size(150.dp, 60.dp)
                    ) {
                        Text("Руководитель")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                            onRegister(email, password, firstName, lastName, selectRole)
                            onDismiss()
                        } else {
                            errorMessage = "Пожалуйста, заполните все поля"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.darkBlue),
                        contentColor = colorResource(id = R.color.white)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Зарегистрировать",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: AppUser,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${user.firstName} ${user.lastName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Email: ${user.email}",
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Пароль: ${user.password}",
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Роль: ${user.role}",
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Удалить")
        }
    }
}

private fun loadUsers(
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
                    email = document.getString("email") ?: "",
                    password = document.getString("password") ?: "",
                    role = document.getString("role") ?: ""
                )
                users.add(user)
            }
            onSuccess(users) // Передаем список пользователей
        }
        .addOnFailureListener { e ->
            onFailure(e) // Передаем ошибку
        }
}

private fun signUp(
    auth: FirebaseAuth,
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    role: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                if (userId != null) {
                    saveUserProfile(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password,
                        role = role,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                } else {
                    onFailure(Exception("Ошибка при создании пользователя"))
                }
            } else {
                onFailure(task.exception ?: Exception("Ошибка при регистрации"))
            }
        }
}

private fun saveUserProfile(
    userId: String,
    firstName: String,
    lastName: String,
    email: String,
    password: String, // Добавлен параметр password
    role: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userProfile = hashMapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "email" to email,
        "password" to password,
        "role" to role
    )

    db.collection("users")
        .document(userId)
        .set(userProfile)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

private fun deleteUser(
    userId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Удаление из Firestore
    db.collection("users")
        .document(userId)
        .delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

