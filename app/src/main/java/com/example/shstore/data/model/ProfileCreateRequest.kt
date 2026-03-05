package com.example.shstore.data.model

data class ProfileCreateRequest(
    val user_id: String,
    val firstname: String,
    val lastname: String,
    val phone: String,
    val address: String,
    val photo: String? = null
)