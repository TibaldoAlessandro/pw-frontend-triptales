package com.example.frontend_triptales

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frontend_triptales.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn.value) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("home") {
            HomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
   }
}