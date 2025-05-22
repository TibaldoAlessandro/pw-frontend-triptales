package com.example.frontend_triptales

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    groupId: Int,
    navController: NavController,
    postViewModel: PostViewModel
) {
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var createdPostId by remember { mutableStateOf<Int?>(null) }

    val isLoading by postViewModel.isLoading
    val isUploadingImage by postViewModel.isUploadingImage
    val message by postViewModel.message
    val context = LocalContext.current

    // Launcher per la selezione dell'immagine
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
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

                Spacer(modifier = Modifier.height(16.dp))

                // Sezione per l'immagine
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
                            Text(
                                text = "Aggiungi immagine",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row {
                                if (selectedImageUri != null) {
                                    IconButton(
                                        onClick = { selectedImageUri = null }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Rimuovi immagine",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Seleziona immagine"
                                    )
                                }
                            }
                        }

                        selectedImageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Immagine selezionata",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (postText.isNotBlank()) {
                            postViewModel.createPost(
                                groupId = groupId,
                                text = postText,
                                onSuccess = {
                                    // Se c'è un'immagine selezionata, la carichiamo dopo aver creato il post
                                    selectedImageUri?.let { uri ->
                                        // Nota: qui dovresti ottenere l'ID del post appena creato
                                        // Per semplicità, assumiamo che il backend restituisca il post con l'ID
                                        // Potresti dover modificare il ViewModel per gestire questo caso
                                    }
                                    navController.popBackStack()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = postText.isNotBlank() && !isLoading && !isUploadingImage
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

                // Indicatore di caricamento immagine
                if (isUploadingImage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Caricamento immagine...",
                            style = MaterialTheme.typography.bodySmall
                        )
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