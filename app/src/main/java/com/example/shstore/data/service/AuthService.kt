package com.example.shstore.data.service

import com.example.shstore.data.model.*
import retrofit2.Response
import retrofit2.http.*

// ── Response models ───────────────────────────────────────────────────────────

data class AuthUserResponse(
    val id: String?,
    val email: String?
)

data class AuthResponse(
    val access_token: String?,
    val refresh_token: String?,
    val token_type: String?,
    val user: AuthUserResponse?
)

// ── Auth Service ──────────────────────────────────────────────────────────────

interface AuthService {

    @POST("signup")
    suspend fun signUp(
        @Body body: SignUpRequest
    ): Response<AuthResponse>

    @POST("token?grant_type=password")
    suspend fun signIn(
        @Body body: SignInRequest
    ): Response<AuthResponse>

    @POST("verify")
    suspend fun verifyOtp(
        @Body body: VerifyOtpRequest
    ): Response<AuthResponse>

    @POST("recover")
    suspend fun recoverPassword(
        @Body body: RecoverRequest
    ): Response<Unit>

    @PUT("user")
    suspend fun updatePassword(
        @Header("Authorization") authHeader: String,
        @Body body: UpdatePasswordRequest
    ): Response<AuthUserResponse>
}

// ── Request models ────────────────────────────────────────────────────────────

data class RecoverRequest(val email: String)
data class UpdatePasswordRequest(val password: String)
