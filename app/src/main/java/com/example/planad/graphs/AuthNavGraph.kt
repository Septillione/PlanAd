package com.example.planad.graphs

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.planad.BottomBarScreen
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.auth.LoginScreen
import com.example.planad.screens.auth.SignUpScreen
import com.example.planad.screens.auth.StartAuthScreen
import com.example.planad.screens.auth.AdminScreen
import com.example.planad.screens.main.SettingsScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    navigation(
        route = Graph.AUTHENTICATION,
        startDestination = AuthScreen.Login.route
    ) {
        composable(route = AuthScreen.Admin.route) {
            AdminScreen(
                onBack = {
                    navController.popBackStack()
                    navController.navigate(Graph.AUTHENTICATION)
                }
            )
        }
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLogin = {
                    navController.popBackStack()
                    navController.navigate(Graph.PROJECT)
                },
                onAdminLogin = {
                    navController.navigate(AuthScreen.Admin.route)
                },
                authViewModel = authViewModel
            )
        }
    }
}

sealed class AuthScreen(val route: String) {
    object Login: AuthScreen(route = "LOGIN")
    object Admin: AuthScreen(route = "ADMIN")
}




