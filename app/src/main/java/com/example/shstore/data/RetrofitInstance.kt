package com.example.shstore.data

import com.example.shstore.data.service.AuthService
import com.example.shstore.data.service.UserManagementService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    const val SUPABASE_URL = "https://siyavxuhmpphhlroshwg.supabase.co/rest/v1/"
    private const val AUTH_URL = "https://siyavxuhmpphhlroshwg.supabase.co/auth/v1/"

    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNpeWF2eHVobXBwaGhscm9zaHdnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI0NTM0OTYsImV4cCI6MjA4ODAyOTQ5Nn0.V0rf2ij993dmBjbSEvfCOVCegjWaCchr0i5Wu7C-MQY"
    private val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("10.207.106.59", 3128))
    private val supabaseInterceptor = Interceptor { chain ->

        val request = chain.request().newBuilder()
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${UserSession.accessToken ?: ""}")
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Profile", "public")
            .addHeader("Content-Profile", "public")
            .addHeader("Content-Type", "application/json")
            .build()

        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .proxy(proxy)
        .addInterceptor(supabaseInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(SUPABASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(AUTH_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val userManagementService: UserManagementService =
        retrofit.create(UserManagementService::class.java)

    val authService: AuthService =
        authRetrofit.create(AuthService::class.java)
}