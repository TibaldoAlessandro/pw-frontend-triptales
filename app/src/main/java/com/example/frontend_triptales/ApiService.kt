package com.example.frontend_triptales

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register/")
    suspend fun registerUser(@Body user: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/login/")
    suspend fun loginUser(@Body credentials: LoginRequest): Response<LoginResponse>

    @GET("api/auth/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>
}

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)