package com.example.planad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.planad.graphs.HomeNavGraph
import com.example.planad.graphs.RootNavigationGraph
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.main.ProjectsScreen
import com.example.planad.ui.theme.PlanAdTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()

        setContent {
            PlanAdTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RootNavigationGraph(
                        navController = rememberNavController(),
                        modifier = Modifier.padding(innerPadding),
                        authViewModel
                    )
                }
            }
        }
    }
}
