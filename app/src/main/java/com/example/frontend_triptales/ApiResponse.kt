package com.example.frontend_triptales

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val user: User,
    val token: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("registration_date") val registrationDate: String  // Ora funzionerà
)

data class ErrorResponse(
    val error: String?
)