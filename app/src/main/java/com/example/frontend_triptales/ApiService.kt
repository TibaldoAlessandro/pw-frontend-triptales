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

    @GET("api/posts/group/{groupId}/")
    suspend fun getGroupPosts(@Path("groupId") groupId: Int): Response<List<Post>>

    @POST("api/posts/")
    suspend fun createPost(@Body postData: PostCreateRequest): Response<Post>

    @DELETE("api/posts/{postId}/")
    suspend fun deletePost(@Path("postId") postId: Int): Response<Unit>

    @GET("api/groups/{groupId}/members/")
    suspend fun getGroupMembers(@Path("groupId") groupId: Int): Response<List<User>>

    @DELETE("api/groups/{groupId}/")
    suspend fun deleteGroup(@Path("groupId") groupId: Int): Response<Unit>
}