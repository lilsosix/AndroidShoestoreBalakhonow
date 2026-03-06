package com.example.shstore.data.service

import com.example.shstore.data.model.*
import retrofit2.Response
import retrofit2.http.*

data class ProfileDto(
    val id: String?,
    val user_id: String?,
    val firstname: String?,
    val lastname: String?,
    val address: String?,
    val phone: String?,
    val photo: String?
)

data class ProductDto(
    val id: String,
    val title: String,
    val cost: Double,
    val description: String,
    val category_id: String?,
    val is_best_seller: Boolean?
)

data class FavouriteDto(
    val id: String?,
    val product_id: String?,
    val user_id: String?
)

data class UpdateCountRequest(val count: Long)

interface UserManagementService {

    @GET("profiles")
    suspend fun getProfile(
        @Query("user_id") userIdFilter: String
    ): List<ProfileDto>

    @POST("profiles")
    suspend fun createProfile(
        @Body body: ProfileCreateRequest
    ): Response<Unit>

    @PATCH("profiles")
    @Headers("Prefer: return=minimal")
    suspend fun updateProfile(
        @Query("user_id") userIdFilter: String,
        @Body body: UpdateProfileRequest
    ): Response<Unit>


    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products")
    suspend fun getProductsByCategory(
        @Query("category_id") categoryIdFilter: String
    ): List<ProductDto>

    @GET("cart")
    suspend fun getCartItems(
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*,products(*)"
    ): List<CartItemDto>

    @POST("cart")
    suspend fun addToCart(
        @Body body: CartRequest
    )

    @PATCH("cart")
    suspend fun updateCartItemCount(
        @Query("id") idFilter: String,
        @Body body: UpdateCountRequest
    ): Response<Unit>

    @DELETE("cart")
    suspend fun removeCartItem(
        @Query("id") idFilter: String
    ): Response<Unit>

    @DELETE("cart")
    suspend fun clearCart(
        @Query("user_id") userIdFilter: String
    ): Response<Unit>

    @GET("favourite")
    suspend fun getFavourites(
        @Query("user_id") userIdFilter: String
    ): List<FavouriteDto>

    @POST("favourite")
    suspend fun addFavourite(
        @Body body: FavouriteRequest
    ): Response<Unit>

    @DELETE("favourite")
    suspend fun deleteFavourite(
        @Query("user_id") userIdFilter: String,
        @Query("product_id") productIdFilter: String
    ): Response<Unit>

    @GET("orders")
    suspend fun getOrders(
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*,order_status(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<OrderDto>

    @POST("orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequest
    ): Response<List<OrderDto>>

    @PATCH("orders")
    suspend fun updateOrderStatus(
        @Query("id") idFilter: String,
        @Body body: Map<String, String?>
    ): Response<Unit>

    @GET("orders_items")
    suspend fun getOrderItems(
        @Query("order_id") orderIdFilter: String
    ): List<OrderItemDto>

    @POST("orders_items")
    suspend fun createOrderItems(
        @Body body: List<CreateOrderItemRequest>
    ): Response<Unit>
}