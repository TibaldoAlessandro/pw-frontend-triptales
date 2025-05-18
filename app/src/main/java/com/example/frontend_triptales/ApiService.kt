package com.example.frontend_triptales

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login/")
    suspend fun loginUser(@Body user: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register/")
    suspend fun registerUser(@Body user: RegisterRequest): Response<LoginResponse>
}