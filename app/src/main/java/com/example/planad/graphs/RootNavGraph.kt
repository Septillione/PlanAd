package com.example.planad.graphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.planad.BottomBarDisplay
import com.example.planad.screens.auth.AuthState
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.main.ProjectsScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

@Composable
fun RootNavigationGraph(navController: NavHostController, modifier: Modifier, authViewModel: AuthViewModel) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val startDestination = if(auth.currentUser == null) {
        Graph.AUTHENTICATION
    } else {
        Graph.PROJECT
    }

    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = startDestination
    ) {
        authNavGraph(navController = navController, authViewModel = authViewModel)
        composable(route = Graph.PROJECT) {
            BottomBarDisplay(authViewModel = authViewModel)
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val PROJECT = "project_graph"
    const val TASK = "task_graph"
}






