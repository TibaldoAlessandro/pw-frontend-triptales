package com.example.frontend_triptales

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {
    // Stato per i gruppi
    private val _groups = mutableStateOf<List<Group>>(emptyList())
    val groups: State<List<Group>> = _groups

    // Stato per gli inviti
    private val _invitations = mutableStateOf<List<GroupInvitation>>(emptyList())
    val invitations: State<List<GroupInvitation>> = _invitations

    // Stato di caricamento
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Messaggio di errore o successo
    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    // Recupera i gruppi dell'utente
    fun fetchUserGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.getUserGroups()
                if (response.isSuccessful) {
                    _groups.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Errore nel caricamento dei gruppi: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Recupera gli inviti dell'utente
    fun fetchUserInvitations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.getUserInvitations()
                if (response.isSuccessful) {
                    _invitations.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Errore nel caricamento degli inviti: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Crea un nuovo gruppo
    fun createGroup(name: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.createGroup(
                    GroupCreateRequest(name, description)
                )
                if (response.isSuccessful) {
                    _message.value = "Gruppo creato con successo"
                    fetchUserGroups() // Aggiorna la lista dei gruppi
                    onSuccess()
                } else {
                    _message.value = "Errore nella creazione del gruppo: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Invita un utente a un gruppo
    fun inviteUserToGroup(groupId: Int, recipientEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.inviteToGroup(
                    groupId,
                    GroupInviteRequest(recipientEmail)
                )
                if (response.isSuccessful) {
                    _message.value = "Invito inviato con successo"
                } else {
                    _message.value = "Errore nell'invio dell'invito: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Errore di rete: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Risponde a un invito (accetta o rifiuta)
    fun respondToInvitation(invitationId: Int, accept: Boolean) {
        val status = if (accept) "ACCEPTED" else "REJECTED"
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.apiService.respondToInvitation(
                    invitationId,
                    InvitationResponseRequest(status)
                )
                if (response.isSuccessful) {
                    _message.value = if (accept) "Invito accettato" else "Invito rifiutato"
                    fetchUserInvitations() // Aggiorna la lista degli inviti
                    if (accept) {
                        fetchUserGroups() // Se l'invito è stato accettato, aggiorna anche i gruppi
                    }
                } else {
                    _message.value = "Errore nella risposta all'invito: ${response.message()}"
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