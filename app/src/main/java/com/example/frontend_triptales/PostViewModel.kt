package com.example.frontend_triptales

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    // Stato per i post
    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    // Stato di caricamento
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Messaggio di errore o successo
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    // Recupera i post di un gruppo specifico
    fun fetchGroupPosts(groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.getGroupPosts(groupId)
                if (response.isSuccessful) {
                    _posts.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Errore nel caricamento dei post: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crea un nuovo post
    fun createPost(groupId: Int, text: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.createPost(
                    PostCreateRequest(groupId, text)
                )
                if (response.isSuccessful) {
                    _message.value = "Post creato con successo"
                    fetchGroupPosts(groupId) // Aggiorna la lista dei post
                    onSuccess()
                } else {
                    _message.value = "Errore nella creazione del post: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Elimina un post
    fun deletePost(postId: Int, groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.deletePost(postId)
                if (response.isSuccessful) {
                    _message.value = "Post eliminato con successo"
                    fetchGroupPosts(groupId) // Aggiorna la lista dei post dopo l'eliminazione
                } else {
                    _message.value = "Errore nell'eliminazione del post: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Pulisci il messaggio
    fun clearMessage() {
        _message.value = null
    }
}