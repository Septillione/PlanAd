package com.example.planad.graphs

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.planad.BottomBarScreen
import com.example.planad.screens.auth.AuthViewModel
import com.example.planad.screens.auth.ForgotScreen
import com.example.planad.screens.auth.LoginScreen
import com.example.planad.screens.auth.SignUpScreen
import com.example.planad.screens.auth.StartAuthScreen
import com.example.planad.screens.main.SettingsScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    navigation(
        route = Graph.AUTHENTICATION,
        startDestination = AuthScreen.StartAuth.route
    ) {
        composable(route = AuthScreen.StartAuth.route) {
            StartAuthScreen(
                onLoginClick = {
                    navController.navigate(AuthScreen.Login.route)
                },
                onSignUpClick = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onInCase = {
                    navController.navigate(Graph.PROJECT)
                },
                authViewModel = authViewModel
            )
        }
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLogin = {
                    navController.popBackStack()
                    navController.navigate(Graph.PROJECT)
                },
                onForgotClick = {
                    navController.navigate(AuthScreen.Forgot.route)
                },
                authViewModel = authViewModel
            )
        }
        composable(route = AuthScreen.SignUp.route) {
            SignUpScreen(
                onSignUp = {
                    navController.popBackStack()
                    navController.navigate(AuthScreen.Login.route)
                },
                authViewModel = authViewModel
            )
        }
        composable(route = AuthScreen.Forgot.route) {
            ForgotScreen(
                onClick = {
                    navController.navigate(AuthScreen.Login.route)
                }
            )
        }
    }
}

sealed class AuthScreen(val route: String) {
    object StartAuth: AuthScreen(route = "START_AUTH")
    object Login: AuthScreen(route = "LOGIN")
    object SignUp: AuthScreen(route = "SIGN_UP")
    object Forgot: AuthScreen(route = "FORGOT")
}




