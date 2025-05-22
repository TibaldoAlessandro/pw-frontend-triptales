package com.example.frontend_triptales

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor

object RetrofitInstance {
    private const val BASE_URL = "https://centrally-innocent-walrus.ngrok-free.app"

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor())
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// Intercettore per aggiungere automaticamente il token di autenticazione
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder().also { request ->
            SharedPrefsManager.getAuthToken()?.let {
                request.addHeader("Authorization", it)
            }
        }.build()
    )
}

// Singleton per gestire le preferenze condivise
object SharedPrefsManager {
    private var authToken: String? = null
    private var currentUser: User? = null

    fun saveAuthToken(token: String) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken

    fun clearAuthToken() {
        authToken = null
        currentUser = null
    }

    fun saveCurrentUser(user: User) {
        currentUser = user
    }

    fun getCurrentUser(): User? = currentUser
}