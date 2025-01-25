package com.example.planad.graphs

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.planad.BottomBarDisplay
import com.example.planad.BottomBarScreen
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.main.ProjectsScreen
import com.example.planad.screens.main.SettingsScreen
import com.example.planad.screens.main.UserTasksScreen

@Composable
fun HomeNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        route = Graph.PROJECT,
        startDestination = BottomBarScreen.Projects.route
    ) {
        composable(route = BottomBarScreen.Projects.route) {
            ProjectsScreen()
        }
        composable(route = BottomBarScreen.UserTasks.route) {
            UserTasksScreen()
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(
                onSignOut = {
                    navController.navigate(Graph.AUTHENTICATION)/* {
                        popUpTo(Graph.PROJECT) { inclusive = true }
                    }*/
                },
                onDeleteAccount = {
                    navController.navigate(Graph.AUTHENTICATION)
                },
                authViewModel = authViewModel
            )
        }
    }
}





