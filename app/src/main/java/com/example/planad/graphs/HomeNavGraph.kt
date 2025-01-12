package com.example.planad.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.planad.BottomBarScreen
import com.example.planad.screens.main.ProjectsScreen
import com.example.planad.screens.main.SettingsScreen
import com.example.planad.screens.main.UserTasksScreen

@Composable
fun HomeNavGraph(navController: NavHostController) {
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
            SettingsScreen()
        }
    }
}






