package com.example.planad.screens.main

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planad.R
import com.example.planad.screens.auth.AuthState
import com.example.planad.screens.auth.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel
) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    var userData by remember { mutableStateOf<AppUser?>(null) }

    // Получаем данные пользователя
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            getUserData(
                userId = userId,
                onSuccess = { user ->
                    userData = user
                },
                onFailure = { e ->
                    Log.e("MyLog", "Ошибка при получении данных пользователя: ${e.message}")
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 100.dp)
        ) {
            Text(
                text = "Настройки",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Отображение имени и фамилии пользователя
            userData?.let { user ->
                Text(
                    text = "Имя: ${user.firstName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Фамилия: ${user.lastName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Должность: ${user.role}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = {
                    signOut(auth, onSignOut)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier.size(width = 250.dp, height = 60.dp)
            ) {
                Text(
                    text = "Выйти",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun signOut(auth: FirebaseAuth, onSignOut: () -> Unit) {
    try {
        auth.signOut()
        onSignOut()
    } catch (e: Exception) {
        Log.e("MyLog", "Ошибка выхода: ${e.message}")
    }
}

fun getUserData(
    userId: String,
    onSuccess: (AppUser) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val user = AppUser(
                    id = document.id,
                    firstName = document.getString("firstName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    email = document.getString("email") ?: "",
                    role = document.getString("role") ?: "",
                    password = "" // Пароль не нужен для отображения
                )
                onSuccess(user)
            } else {
                onFailure(Exception("Пользователь не найден"))
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

