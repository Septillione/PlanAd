package com.example.planad.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material3.TextButton
import com.example.planad.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onAdminLogin: () -> Unit,
    authViewModel: AuthViewModel
) {
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<LoginError?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вход",
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

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.width(300.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            if (loginError != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = loginError!!.message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    loginError = null
                    signIn(auth, email, password, onLogin, onAdminLogin) { error ->
                        loginError = error
                    }
                    //authViewModel.login(email, password)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier.size(width = 250.dp, height = 60.dp)
            ) {
                Text(
                    text = "Войти",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun signIn(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onLogin: () -> Unit,
    onAdminLogin: () -> Unit,
    onError: (LoginError) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MyLog", "Успешный вход")
                if (email == "admin@gmail.com") {
                    onAdminLogin()
                } else {
                    onLogin()
                }
            } else {
                val error = task.exception?.message ?: "Ошибка входа"
                val loginError = getLoginError(error)
                Log.d("MyLog", "Ошибка входа: $error")
                onError(loginError)
            }
        }
}

private fun getLoginError(error: String?): LoginError {
    return when {
        error == null -> LoginError.UNKNOWN_ERROR
        error.contains("The supplied auth credential is incorrect, malformed or has expired.") -> LoginError.INVALID_EMAIL
        error.contains("The password is invalid or the user does not have a password") -> LoginError.INVALID_PASSWORD
        error.contains("There is no user record corresponding to this identifier") -> LoginError.USER_NOT_FOUND
        error.contains("A network error has occurred") -> LoginError.NETWORK_ERROR
        error.contains("Too many unsuccessful login attempts") -> LoginError.TOO_MANY_ATTEMPTS
        else -> LoginError.UNKNOWN_ERROR
    }
}

enum class LoginError(val message: String) {
    INVALID_EMAIL("Ошибка входа"),
    INVALID_PASSWORD("Ошибка входа"),
    USER_NOT_FOUND("Ошибка входа"),
    NETWORK_ERROR("Ошибка сети. Проверьте подключение к интернету"),
    TOO_MANY_ATTEMPTS("Слишком много попыток входа. Попробуйте позже"),
    UNKNOWN_ERROR("Неизвестная ошибка")
}

