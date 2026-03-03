package com.example.shstore.data.service

import com.example.shstore.data.model.*
import retrofit2.Response
import retrofit2.http.*

const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNpeWF2eHVobXBwaGhscm9zaHdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI0NTM0OTYsImV4cCI6MjA4ODAyOTQ5Nn0.V0rf2ij993dmBjbSEvfCOVCegjWaCchr0i5Wu7C-MQY"

interface UserManagementService {

    // ---------- ТОЛЬКО РЕГИСТРАЦИЯ ----------

    @Headers("apikey: $API_KEY", "Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): Response<SignUpResponse>

    @Headers("apikey: ${com.example.shstore.data.service.API_KEY}", "Content-Type: application/json")
    @POST("auth/v1/recover")
    suspend fun recoverPassword(@Body body: Map<String, String>): Response<Any>
}