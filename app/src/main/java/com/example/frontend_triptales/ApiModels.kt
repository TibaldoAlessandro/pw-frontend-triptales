package com.example.frontend_triptales

import java.util.Date

// Modelli esistenti
data class LoginResponse(
    val user: User,
    val token: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Nuovi modelli per gruppi e inviti
data class Group(
    val id: Int,
    val name: String,
    val description: String?,
    val creation_date: String,
    val creator: User
)

data class GroupInvitation(
    val id: Int,
    val group: Group,
    val sender: User,
    val recipient: User,
    val status: String,
    val created_at: String,
    val updated_at: String
)

data class GroupCreateRequest(
    val name: String,
    val description: String
)

data class GroupInviteRequest(
    val recipient: String  // Email dell'utente da invitare
)

data class InvitationResponseRequest(
    val status: String  // ACCEPTED o REJECTED
)

data class Post(
    val id: Int,
    val author: User,
    val group: Group,
    val text: String,
    val created_at: String
)

data class PostCreateRequest(
    val group_id: Int,
    val text: String
)