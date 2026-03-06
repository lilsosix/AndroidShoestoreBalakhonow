package com.example.shstore.data.model

/**
 * DTO профиля пользователя — используется при запросах к таблице profiles.
 * Дата создания: 2025
 * Автор: ShStore Team
 */
data class ProfileDto(
    val id: String?,
    val user_id: String?,
    val photo: String?,
    val firstname: String?,
    val lastname: String?,
    val address: String?,
    val phone: String?
)