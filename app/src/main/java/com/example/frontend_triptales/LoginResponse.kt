package com.example.frontend_triptales

data class LoginResponse(
    val user: User,
    val token: String
)