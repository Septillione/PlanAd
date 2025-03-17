package com.example.planad.graphs

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.planad.BottomBarDisplay
import com.example.planad.BottomBarScreen
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.main.ProjectsScreen
import com.example.planad.screens.main.SettingsScreen
import com.example.planad.screens.main.TasksScreen
import com.example.planad.screens.main.UserTasksScreen

@Composable
fun HomeNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        route = Graph.PROJECT,
        startDestination = BottomBarScreen.Projects.route
    ) {
        composable(route = BottomBarScreen.Projects.route) {
            ProjectsScreen(
                onProjectTap = { projectId ->
                    navController.navigate("${Graph.TASK}/$projectId")
                }
            )
        }
        composable(route = BottomBarScreen.UserTasks.route) {
            UserTasksScreen(
                onBackTap = { navController.navigate(Graph.PROJECT) }
            )
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(
                onSignOut = {
                    Log.d("MyLog", "Колбэк onSignOut вызван")
                    navController.navigate(Graph.AUTHENTICATION) {
                        popUpTo(Graph.PROJECT) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        composable(
            route = "${Graph.TASK}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            if (projectId != null) {
                TasksScreen(
                    onBackTap = {navController.popBackStack()},
                    onExecutorSelect = {},
                    projectId = projectId
                )
            } else {
                Text(text = "Ошибка: projectId не найден", color = Color.Red)
            }
        }
    }
}





