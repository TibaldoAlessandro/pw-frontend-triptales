package com.example.frontend_triptales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Int,
    navController: NavController,
    groupViewModel: GroupViewModel
) {
    val groups by groupViewModel.groups
    val isLoading by groupViewModel.isLoading
    val message by groupViewModel.message

    // Trova il gruppo corrente in base all'ID
    val currentGroup = groups.find { it.id == groupId }

    // Stato per il bottone di invito
    var showInviteDialog by remember { mutableStateOf(false) }
    var emailToInvite by remember { mutableStateOf("") }
    var inviteError by remember { mutableStateOf<String?>(null) }

    // Carica i gruppi se necessario
    LaunchedEffect(Unit) {
        if (groups.isEmpty()) {
            groupViewModel.fetchGroupMembers(groupId)
            groupViewModel.fetchUserGroups()
        }
    }

    // Gestione messaggi
    message?.let {
        LaunchedEffect(it) {
            // Puoi implementare uno snackbar o un toast qui
            groupViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGroup?.name ?: "Dettagli gruppo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Torna indietro")
                    }
                }
            )

            val currentUserId = AuthViewModel.getCurrentUserId()

            if (currentGroup?.creator?.id == currentUserId) {
                Button(
                    onClick = {
                        groupViewModel.deleteGroup(currentGroup.id) {
                            navController.popBackStack() // Torna alla lista gruppi
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Elimina gruppo", color = MaterialTheme.colorScheme.onError)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showInviteDialog = true }
            ) {
                Icon(Icons.Default.Person, contentDescription = "Invita utente")
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
            } else if (currentGroup == null) {
                Text(
                    text = "Gruppo non trovato",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Informazioni sul gruppo
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentGroup.name,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            currentGroup.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = "Creato da: ${currentGroup.creator.username}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Data creazione: ${currentGroup.creation_date}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pulsante per visualizzare i post del gruppo
                    Button(
                        onClick = { navController.navigate("group_posts/$groupId") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Post"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visualizza post del gruppo")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista dei membri
                    Text(
                        text = "Membri del gruppo",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Per ora mostriamo solo il creatore come membro
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Usa items con la lista dei membri
                        val members by groupViewModel.groupMembers

                        items(members) { user ->
                            MemberItem(user = user)
                        }

                    }
                }
            }
        }
    }

    // Dialog per invitare utenti
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invita un utente") },
            text = {
                Column {
                    Text("Inserisci l'email dell'utente da invitare")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailToInvite,
                        onValueChange = {
                            emailToInvite = it
                            inviteError = null
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = inviteError != null
                    )

                    inviteError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailToInvite.isNotBlank() && currentGroup != null) {
                            // Valida l'email
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailToInvite).matches()) {
                                inviteError = "Email non valida"
                            } else {
                                groupViewModel.inviteUserToGroup(currentGroup.id, emailToInvite)
                                emailToInvite = ""
                                inviteError = null
                                showInviteDialog = false
                            }
                        } else if (emailToInvite.isBlank()) {
                            inviteError = "Inserisci un'email"
                        }
                    }
                ) {
                    Text("Invita")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showInviteDialog = false
                        inviteError = null
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun MemberItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium
                )
                user.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}