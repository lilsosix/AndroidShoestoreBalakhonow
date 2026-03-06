package com.example.shstore.ui.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.ProfileCreateRequest
import com.example.shstore.data.model.VerifyOtpRequest
import kotlinx.coroutines.launch

class VerifyOTPViewModel : ViewModel() {

    val isLoading = mutableStateOf(false)
    val isVerified = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun verifyOTP(
        email: String,
        otp: String,
        type: String,
        name: String = "",
        password: String = "",
        context: Context,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val authService = RetrofitInstance.authService

                val resp = authService.verifyOtp(
                    VerifyOtpRequest(email = email, token = otp, type = type)
                )

                if (resp.isSuccessful) {
                    val body = resp.body()
                    // Исправлено: body?.access_token уже String? — не Any?
                    val accessToken: String? = body?.access_token
                    val userId: String? = body?.user?.id

                    if (accessToken != null && userId != null) {
                        UserSession.accessToken = accessToken
                        UserSession.userId = userId
                        UserSession.email = email

                        when (type) {
                            "signup" -> {
                                // Создаём профиль при регистрации
                                if (name.isNotBlank()) {
                                    try {
                                        RetrofitInstance.userManagementService.createProfile(
                                            body = ProfileCreateRequest(
                                                user_id = userId,
                                                firstname = name,
                                                lastname = "",
                                                phone = "",
                                                address = ""
                                            )
                                        )
                                    } catch (_: Exception) {
                                        // Профиль не критичен
                                    }
                                }
                                isVerified.value = true
                                navController.navigate("home") {
                                    popUpTo("onboard1") { inclusive = true }
                                }
                            }

                            "recovery" -> {
                                // После верификации OTP для сброса пароля →
                                // переходим на экран нового пароля
                                navController.navigate("new_password/$email") {
                                    popUpTo("forgot_password") { inclusive = true }
                                }
                            }

                            else -> {
                                isVerified.value = true
                                navController.navigate("home") {
                                    popUpTo("onboard1") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        errorMessage.value = "Не удалось получить сессию"
                    }
                } else {
                    val code = resp.code()
                    errorMessage.value = when (code) {
                        400 -> "Неверный или устаревший код"
                        422 -> "Некорректный формат кода"
                        else -> "Ошибка верификации: $code"
                    }
                }

            } catch (e: java.io.IOException) {
                errorMessage.value = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage.value = "Ошибка: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun resetVerificationState() {
        isVerified.value = false
    }

    fun clearError() {
        errorMessage.value = null
    }
}
