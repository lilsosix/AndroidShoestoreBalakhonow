package com.example.shstore.data.model

// ── Cart ──────────────────────────────────────────────────────────────────────

data class CartRequest(
    val user_id: String,
    val product_id: String,
    val count: Int = 1
)

data class CartItemDto(
    val id: String,
    val product_id: String?,
    val user_id: String?,
    val count: Long?,
    val products: ProductEmbedded?
)

data class ProductEmbedded(
    val id: String?,
    val title: String,
    val cost: Double,
    val description: String = ""
)

// ── Orders ────────────────────────────────────────────────────────────────────

data class CreateOrderRequest(
    val email: String,
    val phone: String,
    val address: String,
    val user_id: String,
    val delivery_coast: Long = 60L
)

data class CreateOrderItemRequest(
    val title: String,
    val coast: Double,
    val count: Long,
    val order_id: Long,
    val product_id: String?
)

data class OrderDto(
    val id: Long,
    val created_at: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val user_id: String?,
    val delivery_coast: Long?,
    val status_id: String?,
    val order_status: OrderStatusEmbedded?
)

data class OrderStatusEmbedded(
    val id: String?,
    val name: String?
)

data class OrderItemDto(
    val id: String?,
    val title: String?,
    val coast: Double?,
    val count: Long?,
    val order_id: Long?,
    val product_id: String?
)

// ── Profile update ────────────────────────────────────────────────────────────

data class UpdateProfileRequest(
    val firstname: String,
    val lastname: String,
    val address: String,
    val phone: String
)