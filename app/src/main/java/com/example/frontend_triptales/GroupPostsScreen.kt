package com.example.frontend_triptales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
            // Puoi implementare uno snackbar o un toast qui
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
                    Button(onClick = { navController.navigate("create_post/$groupId") }) {
                        Text("Crea il primo post")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            isAuthor = post.author.id == currentUserId,
                            onDelete = { postViewModel.deletePost(post.id, groupId) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItem(
    post: Post,
    isAuthor: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = post.author.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = post.created_at,
                        style = MaterialTheme.typography.bodySmall
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}