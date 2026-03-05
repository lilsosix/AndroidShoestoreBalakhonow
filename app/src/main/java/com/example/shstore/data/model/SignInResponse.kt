package com.example.shstore.data.model

data class SignInResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: User
)

data class User(
    val id: String,
    val email: String
)