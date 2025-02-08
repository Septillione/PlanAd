package com.example.planad.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planad.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun SignUpScreen(
    onSignUp: () -> Unit,
    authViewModel: AuthViewModel,
) {
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(50.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.width(300.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(100.dp))

            Button(
                onClick = {
                    signUp(auth, email, password, onSignUp)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier.size(width = 250.dp, height = 60.dp)
            ) {
                Text(
                    text = "Зарегистрироваться",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun signUp(auth: FirebaseAuth, email: String, password: String, onSignUp: () -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("MyLog", "Успешная регистрация")
                onSignUp()
            } else {
                Log.d("MyLog", "Регистрация накрылась")
            }
        }
}

@Preview
@Composable
fun PreviewSignUp() {
    SignUpScreen(onSignUp = {}, authViewModel = AuthViewModel())
}





