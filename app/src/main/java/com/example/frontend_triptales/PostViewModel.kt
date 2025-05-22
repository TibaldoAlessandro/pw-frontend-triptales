package com.example.frontend_triptales

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class PostViewModel : ViewModel() {
    // Stato per i post
    private val _posts = mutableStateOf<List<Post>>(emptyList())
    val posts: State<List<Post>> = _posts

    // Stato per i commenti
    private val _comments = mutableStateOf<List<Comment>>(emptyList())
    val comments: State<List<Comment>> = _comments

    // Stato di caricamento
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Messaggio di errore o successo
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    // Stato per l'upload delle immagini
    private val _isUploadingImage = mutableStateOf(false)
    val isUploadingImage: State<Boolean> = _isUploadingImage

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

    // Upload immagine per un post esistente
    fun uploadImageToPost(
        context: Context,
        postId: Int,
        imageUri: Uri,
        groupId: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                // Converte URI in File
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Prepara i parametri per la richiesta multipart
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val postRequestBody = postId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val latRequestBody = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val lngRequestBody = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitInstance.apiService.uploadPhoto(
                    postRequestBody, imagePart, latRequestBody, lngRequestBody
                )

                if (response.isSuccessful) {
                    _message.value = "Immagine caricata con successo"
                    fetchGroupPosts(groupId) // Ricarica i post per mostrare l'immagine
                    file.delete() // Pulisce il file temporaneo
                } else {
                    _message.value = "Errore nel caricamento dell'immagine: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore nel caricamento dell'immagine: ${e.message}"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    // Toggle like su un post
    fun toggleLike(postId: Int, groupId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.toggleLike(postId)
                if (response.isSuccessful) {
                    // Aggiorna il post nella lista locale
                    val updatedPosts = _posts.value.map { post ->
                        if (post.id == postId) {
                            response.body()?.let { likeResponse ->
                                post.copy(
                                    likes_count = likeResponse.likes_count,
                                    user_has_liked = likeResponse.liked
                                )
                            } ?: post
                        } else {
                            post
                        }
                    }
                    _posts.value = updatedPosts
                } else {
                    _message.value = "Errore nell'operazione like: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Recupera i commenti di un post
    fun fetchPostComments(postId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getPostComments(postId)
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Errore nel caricamento dei commenti: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Crea un nuovo commento
    fun createComment(postId: Int, text: String, groupId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.createComment(
                    CommentCreateRequest(postId, text)
                )
                if (response.isSuccessful) {
                    _message.value = "Commento aggiunto"
                    fetchPostComments(postId) // Ricarica i commenti
                    fetchGroupPosts(groupId) // Ricarica i post per aggiornare il conteggio commenti
                } else {
                    _message.value = "Errore nella creazione del commento: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
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

    // Elimina un commento
    fun deleteComment(commentId: Int, postId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.deleteComment(commentId)
                if (response.isSuccessful) {
                    _message.value = "Commento eliminato"
                    fetchPostComments(postId) // Ricarica i commenti
                } else {
                    _message.value = "Errore nell'eliminazione del commento: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Pulisci il messaggio
    fun clearMessage() {
        _message.value = null
    }

    // Pulisci i commenti
    fun clearComments() {
        _comments.value = emptyList()
    }
}