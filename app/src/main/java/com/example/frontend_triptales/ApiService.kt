package com.example.frontend_triptales

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login/")
    suspend fun loginUser(@Body user: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register/")
    suspend fun registerUser(@Body user: RegisterRequest): Response<LoginResponse>

    // API per i gruppi
    @GET("api/groups/my-groups/")
    suspend fun getUserGroups(): Response<List<Group>>

    @GET("api/groups/invitations/")
    suspend fun getUserInvitations(): Response<List<GroupInvitation>>

    @POST("api/groups/")
    suspend fun createGroup(@Body groupData: GroupCreateRequest): Response<Group>

    @POST("api/groups/{groupId}/invite/")
    suspend fun inviteToGroup(
        @Path("groupId") groupId: Int,
        @Body invitationData: GroupInviteRequest
    ): Response<GroupInvitation>

    @PATCH("api/groups/invitations/{invitationId}/respond/")
    suspend fun respondToInvitation(
        @Path("invitationId") invitationId: Int,
        @Body responseData: InvitationResponseRequest
    ): Response<GroupInvitation>
}