package com.example.frontend_triptales

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    groupId: Int,
    navController: NavController,
    postViewModel: PostViewModel
) {
    var postText by remember { mutableStateOf("") }
    val isLoading by postViewModel.isLoading
    val message by postViewModel.message

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
                title = { Text("Crea nuovo post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Scrivi un nuovo post",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    label = { Text("Testo del post") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    isError = postText.isEmpty()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (postText.isNotBlank()) {
                            postViewModel.createPost(
                                groupId = groupId,
                                text = postText,
                                onSuccess = {
                                    // Naviga alla schermata dei post dopo la creazione
                                    navController.popBackStack()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = postText.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Pubblica post")
                    }
                }

                // Mostra il messaggio di errore se presente
                message?.let {
                    if (it.contains("Errore", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}