package com.example.frontend_triptales

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val groupViewModel: GroupViewModel = viewModel(factory = GroupViewModelFactory())

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
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Nuove rotte per gestione gruppi e inviti
        composable("groups") {
            GroupsScreen(
                navController = navController,
                groupViewModel = groupViewModel
            )
        }

        composable("create_group") {
            CreateGroupScreen(
                navController = navController,
                groupViewModel = groupViewModel
            )
        }

        composable("group_detail/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: -1
            GroupDetailScreen(
                groupId = groupId,
                navController = navController,
                groupViewModel = groupViewModel
            )
        }

        composable("invitations") {
            InvitationsScreen(
                navController = navController,
                groupViewModel = groupViewModel
            )
        }
    }
}