package com.example.shstore.data.service

import com.example.shstore.data.model.*
import retrofit2.Response
import retrofit2.http.*

const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNpeWF2eHVobXBwaGhscm9zaHdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI0NTM0OTYsImV4cCI6MjA4ODAyOTQ5Nn0.V0rf2ij993dmBjbSEvfCOVCegjWaCchr0i5Wu7C-MQY"
data class ProfileDto(
    val id: String?,
    val user_id: String?,
    val photo: String?,
    val firstname: String?,
    val lastname: String?,
    val address: String?,
    val phone: String?
)

data class FavouriteDto(
    val id: String?,
    val product_id: String?,
    val user_id: String?
)

data class ProductDto(
    val id: String,
    val title: String,
    val category_id: String?,
    val cost: Double,
    val description: String,
    val is_best_seller: Boolean?
)


interface UserManagementService {

    // ---------- РЕГИСТРАЦИЯ ----------
    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    // ---------- ВХОД ----------
    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body request: SignInRequest): Response<SignInResponse>

    // ---------- ВОССТАНОВЛЕНИЕ ПАРОЛЯ ----------
    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/recover")
    suspend fun recoverPassword(@Body body: Map<String, String>): Response<Any>

    // ---------- ПРОВЕРКА OTP ----------
    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/verify")
    suspend fun verifyOTP(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    // ---------- ИЗМЕНЕНИЕ ПАРОЛЯ ----------
    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @PUT("auth/v1/user")
    suspend fun changePassword(
        @Header("Authorization") authHeader: String,
        @Body request: ChangePasswordRequest
    ): Response<Any>


    // ---------- PROFILES ----------

    @Headers("apikey: ${API_KEY}")
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String, // "eq.<uuid>"
        @Query("select") select: String = "*"
    ): List<ProfileDto>

    @Headers("apikey: ${API_KEY}", "Content-Type: application/json")
    @PUT("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String,
        @Body body: Map<String, Any?>
    ): Response<Unit>
    @Headers("apikey: $API_KEY", "Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/profiles")
    suspend fun createProfile(
        @Header("Authorization") authHeader: String,
        @Body body: ProfileCreateRequest
    ): Response<ProfileDto>


    // ---------- PRODUCTS ----------

    @Headers("apikey: ${API_KEY}")
    @GET("rest/v1/products")
    suspend fun getProducts(
        @Header("Authorization") authHeader: String,
        @Query("select") select: String = "*"
    ): List<ProductDto>
    @Headers("apikey: ${API_KEY}")
    @GET("rest/v1/favourite")
    suspend fun getFavourites(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String, // "eq.<uuid>"
        @Query("select") select: String = "id,product_id,user_id"
    ): List<FavouriteDto>

    @Headers("apikey: ${API_KEY}", "Content-Type: application/json")
    @POST("rest/v1/favourite")
    suspend fun addFavourite(
        @Header("Authorization") authHeader: String,
        @Body body: FavouriteRequest
    ): Response<Unit>

    @Headers("apikey: ${API_KEY}")
    @DELETE("rest/v1/favourite")
    suspend fun deleteFavourite(
        @Header("Authorization") authHeader: String,
        @Query("user_id") userIdFilter: String, // "eq.<uuid>"
        @Query("product_id") productIdFilter: String // "eq.<uuid>"
    ): Response<Unit>
}