package com.example.frontend_triptales

import android.content.Context
import android.net.Uri
import android.util.Log
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
                Log.d("PostViewModel", "Fetching posts for group: $groupId")
                val response = RetrofitInstance.apiService.getGroupPosts(groupId)
                Log.d("PostViewModel", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    _posts.value = response.body() ?: emptyList()
                    Log.d("PostViewModel", "Posts loaded: ${_posts.value.size}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error loading posts: $errorBody")
                    _message.value = "Errore nel caricamento dei post: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error loading posts", e)
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crea un nuovo post con immagine opzionale - VERSIONE CORRETTA
    fun createPostWithImage(
        groupId: Int,
        text: String,
        imageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("PostViewModel", "Creating post for group: $groupId, text: $text")

            try {
                // Verifica che il gruppo ID sia valido
                if (groupId <= 0) {
                    _message.value = "ID gruppo non valido"
                    _isLoading.value = false
                    return@launch
                }

                // Verifica che il testo non sia vuoto
                if (text.trim().isEmpty()) {
                    _message.value = "Il testo del post non può essere vuoto"
                    _isLoading.value = false
                    return@launch
                }

                // Crea la richiesta del post
                val postRequest = PostCreateRequest(groupId, text.trim())
                Log.d("PostViewModel", "Post request: groupId=$groupId, text=${text.trim()}")

                // Prima crea il post
                val response = RetrofitInstance.apiService.createPost(postRequest)
                Log.d("PostViewModel", "Create post response code: ${response.code()}")

                if (response.isSuccessful) {
                    val createdPost = response.body()
                    Log.d("PostViewModel", "Post created successfully: ${createdPost?.id}")

                    if (createdPost != null && imageUri != null) {
                        // Se c'è un'immagine, caricala
                        Log.d("PostViewModel", "Uploading image for post: ${createdPost.id}")
                        _isUploadingImage.value = true

                        uploadImageToPostInternal(
                            context = context,
                            postId = createdPost.id,
                            imageUri = imageUri,
                            groupId = groupId,
                            onComplete = { success ->
                                _isUploadingImage.value = false
                                if (success) {
                                    _message.value = "Post con immagine creato con successo"
                                    Log.d("PostViewModel", "Image uploaded successfully")
                                } else {
                                    _message.value = "Post creato, ma errore nel caricamento dell'immagine"
                                    Log.w("PostViewModel", "Post created but image upload failed")
                                }
                                onSuccess()
                            }
                        )
                    } else {
                        // Post senza immagine
                        _message.value = "Post creato con successo"
                        Log.d("PostViewModel", "Post created without image")
                        fetchGroupPosts(groupId) // Aggiorna la lista dei post
                        onSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error creating post: $errorBody")
                    _message.value = "Errore nella creazione del post: ${response.message()}"

                    // Aggiungi dettagli dell'errore per debug
                    if (errorBody != null) {
                        Log.e("PostViewModel", "Error details: $errorBody")
                        _message.value = "Errore nella creazione del post: $errorBody"
                    }
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error creating post", e)
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Versione interna dell'upload immagine con callback
    private suspend fun uploadImageToPostInternal(
        context: Context,
        postId: Int,
        imageUri: Uri,
        groupId: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            Log.d("PostViewModel", "Starting image upload for post: $postId")

            // Converte URI in File
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("PostViewModel", "Cannot open input stream for image")
                _message.value = "Impossibile leggere l'immagine selezionata"
                onComplete(false)
                return
            }

            val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("PostViewModel", "Image file created: ${file.absolutePath}, size: ${file.length()}")

            // Prepara i parametri per la richiesta multipart
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val postRequestBody = postId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latRequestBody = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lngRequestBody = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitInstance.apiService.uploadPhoto(
                postRequestBody, imagePart, latRequestBody, lngRequestBody
            )

            Log.d("PostViewModel", "Upload photo response code: ${response.code()}")

            if (response.isSuccessful) {
                Log.d("PostViewModel", "Image uploaded successfully")
                fetchGroupPosts(groupId) // Ricarica i post per mostrare l'immagine
                file.delete() // Pulisce il file temporaneo
                onComplete(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PostViewModel", "Error uploading image: $errorBody")
                _message.value = "Errore nel caricamento dell'immagine: ${response.message()}"
                onComplete(false)
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "Exception uploading image", e)
            _message.value = "Errore nel caricamento dell'immagine: ${e.message}"
            onComplete(false)
        }
    }

    // Toggle like su un post
    fun toggleLike(postId: Int, groupId: Int) {
        viewModelScope.launch {
            try {
                Log.d("PostViewModel", "Toggling like for post: $postId")
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
                    Log.d("PostViewModel", "Like toggled successfully")
                } else {
                    Log.e("PostViewModel", "Error toggling like: ${response.message()}")
                    _message.value = "Errore nell'operazione like: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error toggling like", e)
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Recupera i commenti di un post
    fun fetchPostComments(postId: Int) {
        viewModelScope.launch {
            try {
                Log.d("PostViewModel", "Fetching comments for post: $postId")
                val response = RetrofitInstance.apiService.getPostComments(postId)

                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                    Log.d("PostViewModel", "Comments loaded: ${_comments.value.size}")
                } else {
                    Log.e("PostViewModel", "Error loading comments: ${response.message()}")
                    _message.value = "Errore nel caricamento dei commenti: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error loading comments", e)
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Crea un nuovo commento
    fun createComment(postId: Int, text: String, groupId: Int) {
        viewModelScope.launch {
            try {
                Log.d("PostViewModel", "Creating comment for post: $postId")
                val response = RetrofitInstance.apiService.createComment(
                    CommentCreateRequest(postId, text.trim())
                )

                if (response.isSuccessful) {
                    _message.value = "Commento aggiunto"
                    Log.d("PostViewModel", "Comment created successfully")
                    fetchPostComments(postId) // Ricarica i commenti
                    fetchGroupPosts(groupId) // Ricarica i post per aggiornare il conteggio commenti
                } else {
                    Log.e("PostViewModel", "Error creating comment: ${response.message()}")
                    _message.value = "Errore nella creazione del commento: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error creating comment", e)
                _message.value = "Errore di rete: ${e.message}"
            }
        }
    }

    // Elimina un post
    fun deletePost(postId: Int, groupId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("PostViewModel", "Deleting post: $postId")
                val response = RetrofitInstance.apiService.deletePost(postId)

                if (response.isSuccessful) {
                    _message.value = "Post eliminato con successo"
                    Log.d("PostViewModel", "Post deleted successfully")
                    fetchGroupPosts(groupId) // Aggiorna la lista dei post dopo l'eliminazione
                } else {
                    Log.e("PostViewModel", "Error deleting post: ${response.message()}")
                    _message.value = "Errore nell'eliminazione del post: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error deleting post", e)
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
                Log.d("PostViewModel", "Deleting comment: $commentId")
                val response = RetrofitInstance.apiService.deleteComment(commentId)

                if (response.isSuccessful) {
                    _message.value = "Commento eliminato"
                    Log.d("PostViewModel", "Comment deleted successfully")
                    fetchPostComments(postId) // Ricarica i commenti
                } else {
                    Log.e("PostViewModel", "Error deleting comment: ${response.message()}")
                    _message.value = "Errore nell'eliminazione del commento: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Network error deleting comment", e)
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