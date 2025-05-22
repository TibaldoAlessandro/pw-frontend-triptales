package com.example.frontend_triptales

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPostsScreen(
    groupId: Int,
    navController: NavController,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel
) {
    val groups by groupViewModel.groups
    val posts by postViewModel.posts
    val isLoading by postViewModel.isLoading
    val message by postViewModel.message
    val currentUserId = AuthViewModel.getCurrentUserId()

    // Trova il gruppo corrente in base all'ID
    val currentGroup = groups.find { it.id == groupId }

    // Carica i post quando la schermata diventa attiva
    LaunchedEffect(Unit) {
        postViewModel.fetchGroupPosts(groupId)
    }

    // Gestione messaggi
    message?.let {
        LaunchedEffect(it) {
            postViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post di ${currentGroup?.name ?: "Gruppo"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_post/$groupId") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crea post")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Non ci sono post in questo gruppo",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        navController.navigate("create_post/$groupId")
                    }) {
                        Text("Crea il primo post")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        EnhancedPostItem(
                            post = post,
                            isAuthor = post.author.id == currentUserId,
                            onDelete = { postViewModel.deletePost(post.id, groupId) },
                            onLike = { postViewModel.toggleLike(post.id, groupId) },
                            onComment = { navController.navigate("post_comments/${post.id}") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPostItem(
    post: Post,
    isAuthor: Boolean,
    onDelete: () -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del post con info autore e azioni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = post.author.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.created_at,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAuthor) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina post",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Testo del post
            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyLarge
            )

            // Immagini del post
            if (post.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                post.photos.forEach { photo ->
                    Image(
                        painter = rememberAsyncImagePainter(photo.image),
                        contentDescription = "Foto del post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (post.photos.indexOf(photo) < post.photos.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistiche (likes e commenti)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.likes_count > 0) {
                    Text(
                        text = "${post.likes_count} ${if (post.likes_count == 1) "like" else "likes"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (post.likes_count > 0 && post.comments.isNotEmpty()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (post.comments.isNotEmpty()) {
                    Text(
                        text = "${post.comments.size} ${if (post.comments.size == 1) "commento" else "commenti"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action bar (like e commenti)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottone Like
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (post.user_has_liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (post.user_has_liked) "Rimuovi like" else "Aggiungi like",
                            tint = if (post.user_has_liked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bottone Commenti
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onComment) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Visualizza commenti",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mostra alcuni commenti (max 2)
            if (post.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                post.comments.take(2).forEach { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = comment.author.username,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comment.text,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (post.comments.size > 2) {
                    TextButton(
                        onClick = onComment,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = "Visualizza tutti i ${post.comments.size} commenti",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
