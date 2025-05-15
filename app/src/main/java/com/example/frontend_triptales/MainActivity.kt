package com.example.frontend_triptales

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_triptales.ui.theme.FrontendtriptalesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrontendtriptalesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(this)
                    )
                    NavGraph(authViewModel = authViewModel)
                }
            }
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(context) as T
    }
}