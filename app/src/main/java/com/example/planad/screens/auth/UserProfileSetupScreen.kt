package com.example.planad.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planad.R
import com.example.planad.graphs.AuthScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserProfileSetupScreen(
    onProfileComplete: () -> Unit
) {
    var firstName by remember { mutableStateOf("")}
    var lastName by remember { mutableStateOf("") }
    var selectRole by remember { mutableStateOf("Сотрудник") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Настройка профиля",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(30.dp))

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
                if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    saveUserProfile(
                        firstName = firstName,
                        lastName = lastName,
                        role = selectRole,
                        onSuccess = onProfileComplete,
                        onFailure = {
                            errorMessage = "Ошибка: ${it.message}"
                        }
                    )
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
                .size(250.dp, 60.dp)
        ){
            Text(
                text = "Сохранить",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
fun saveUserProfile(
    firstName: String,
    lastName: String,
    role: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userProfile = hashMapOf(
            "firesName" to firstName,
            "lastName" to lastName,
            "role" to role
        )

        db.collection("users").document(user.uid)
            .set(userProfile)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    } else {
        onFailure(Exception("Пользователь не авторизован"))
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileSetupScreenPreview() {
    UserProfileSetupScreen(onProfileComplete = {})
}



