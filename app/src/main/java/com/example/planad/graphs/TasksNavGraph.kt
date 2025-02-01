package com.example.planad.graphs

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.main.Project
import com.example.planad.screens.main.TasksScreen


fun NavGraphBuilder.tasksNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    navigation(
        route = Graph.TASK,
        startDestination = TasksScreen.TaskScreen.route + "/{projectId}"
    ) {
        composable(route = TasksScreen.TaskScreen.route + "/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")

            if (projectId != null) {
                TasksScreen(
                    onBackTap = {
                        navController.navigate(Graph.PROJECT)
                    },
                    onExecutorSelect = {
                        navController.navigate(TasksScreen.ExecutorsScreen.route)
                    },
                    projectId = projectId
                )
            } else {
                Text(
                    text = "Ошибка: projectId не найден",
                    color = Color.Red
                )
            }
        }
    }
}

sealed class TasksScreen(val route: String) {
    object TaskScreen: TasksScreen(route = "TASK_SCREEN")
    object ExecutorsScreen: TasksScreen(route = "EXECUTORS_SCREEN")
}




