package com.example.shstore.data.model

data class ChangePasswordRequest(
    val email: String,
    val newPassword: String
)
