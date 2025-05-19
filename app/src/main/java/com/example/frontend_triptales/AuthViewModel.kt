package com.example.frontend_triptales

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    init {
        _isLoggedIn.value = checkAuthToken()
    }

    private fun checkAuthToken(): Boolean {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", null)
        if (token != null) {
            SharedPrefsManager.saveAuthToken(token)  // Sincronizza con il nostro singleton
            return true
        }
        return false
    }

    private fun saveAuthToken(token: String) {
        val tokenWithPrefix = "Token $token"
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("auth_token", tokenWithPrefix)
            apply()
        }
        SharedPrefsManager.saveAuthToken(tokenWithPrefix)  // Salva anche nel singleton
    }

    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.loginUser(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful) {
                    response.body()?.let {
                        saveAuthToken(it.token)
                        _isLoggedIn.value = true
                        onSuccess()
                    } ?: onError("Invalid response")
                } else {
                    onError(response.errorBody()?.string() ?: "Login failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Network error")
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.registerUser(
                    RegisterRequest(username, email, password)
                )

                if (response.isSuccessful) {
                    // Solo successo, senza login automatico
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: "Registration failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Network error")
            }
        }
    }

    fun logout() {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            remove("auth_token")
            apply()
        }
        SharedPrefsManager.clearAuthToken()  // Pulisci anche il singleton
        _isLoggedIn.value = false
    }
}