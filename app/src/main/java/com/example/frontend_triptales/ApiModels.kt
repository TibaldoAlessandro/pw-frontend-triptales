package com.example.frontend_triptales

data class LoginResponse(
    val user: User,
    val token: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)