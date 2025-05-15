package com.example.frontend_triptales

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn.value) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") },
                authViewModel = authViewModel
            )
        }
        composable("register") {
            // Assicurati di aver creato RegisterScreen.kt
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("home") },
                onNavigateToLogin = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable("home") {
            HomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate("login")
            })
        }
    }
}