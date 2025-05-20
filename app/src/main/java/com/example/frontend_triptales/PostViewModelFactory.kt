package com.example.frontend_triptales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}