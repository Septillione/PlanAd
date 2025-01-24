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

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    authViewModel: AuthViewModel
) {
    val auth = Firebase.auth

//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column{
            Text(
                text = "Настройки",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 100.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    signOut(auth, onSignOut)
//                    authViewModel.signOut()
//                    onSignOut()
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

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    //deleteAccount(auth, email, password, onDeleteAccount)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.darkBlue),
                    contentColor = colorResource(id = R.color.white)
                ),
                modifier = Modifier.size(width = 250.dp, height = 60.dp)
            ) {
                Text(
                    text = "нет",
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
    }
    catch (e: Exception) {
        Log.e("MyLog", "Ошибка выхода: ${e.message}")
    }
}

private fun deleteAccount(auth: FirebaseAuth, email: String, password: String, onDeleteAccount: () -> Unit) {
    val credential = EmailAuthProvider.getCredential(email, password)
    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener{
        if (it.isSuccessful) {
            auth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("MyLog", "Успешное удаление")
                    onDeleteAccount()
                } else {
                    Log.d("MyLog", "Не удалил(")
                }
            }
        } else {
            Log.d("MyLog", "Реаунт.. не прошла")
        }
    }
}

@Preview
@Composable
fun PreviewSettings() {
    //SettingsScreen(onSignOut = {}, onDeleteAccount = {})
}




