package com.example.frontend_triptales

import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    // CORREZIONE: Assicurati che l'endpoint sia corretto
    @POST("api/posts/")
    @Headers("Content-Type: application/json")
    suspend fun createPost(@Body postData: PostCreateRequest): Response<Post>

    @DELETE("api/posts/{postId}/")
    suspend fun deletePost(@Path("postId") postId: Int): Response<Unit>

    @GET("api/groups/{groupId}/members/")
    suspend fun getGroupMembers(@Path("groupId") groupId: Int): Response<List<User>>

    @DELETE("api/groups/{groupId}/")
    suspend fun deleteGroup(@Path("groupId") groupId: Int): Response<Unit>

    // API per le foto
    @Multipart
    @POST("api/photos/upload/")
    suspend fun uploadPhoto(
        @Part("post") postId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?
    ): Response<Photo>

    // API per i like
    @POST("api/posts/{postId}/toggle-like/")
    suspend fun toggleLike(@Path("postId") postId: Int): Response<LikeResponse>

    // API per i commenti
    @GET("api/comments/post/{postId}/")
    suspend fun getPostComments(@Path("postId") postId: Int): Response<List<Comment>>

    @POST("api/comments/")
    @Headers("Content-Type: application/json")
    suspend fun createComment(@Body commentData: CommentCreateRequest): Response<Comment>

    @DELETE("api/comments/{commentId}/")
    suspend fun deleteComment(@Path("commentId") commentId: Int): Response<Unit>
}