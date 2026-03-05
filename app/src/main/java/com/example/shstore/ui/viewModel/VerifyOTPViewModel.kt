package com.example.shstore.ui.viewModel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.*
import kotlinx.coroutines.launch
import java.io.IOException

class VerifyOTPViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isVerified = mutableStateOf(false)

    fun verifyOTP(
        email: String,
        otp: String,
        type: String,
        name: String,
        password: String,
        context: Context,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                isVerified.value = false

                // 1. Подтверждение OTP
                val verifyResponse = RetrofitInstance.userManagementService.verifyOTP(
                    VerifyOtpRequest(email, otp, type)
                )
                if (verifyResponse.isSuccessful) {
                    // 2. Вход с реальным паролем
                    val signInResponse = RetrofitInstance.userManagementService.signIn(
                        SignInRequest(email, password)
                    )
                    if (signInResponse.isSuccessful) {
                        val signInBody = signInResponse.body()
                        val accessToken = signInBody?.access_token
                        val userId = signInBody?.user?.id

                        if (accessToken != null && userId != null) {
                            // Сохраняем токен и userId в сессию
                            UserSession.accessToken = accessToken
                            UserSession.userId = userId

                            // 3. Если это регистрация, создаём профиль
                            if (type == "signup") {
                                val profileRequest = ProfileCreateRequest(
                                    user_id = userId,
                                    firstname = name,
                                    lastname = "",
                                    phone = "",
                                    address = ""
                                )
                                val profileResponse = RetrofitInstance.userManagementService.createProfile(
                                    authHeader = "Bearer $accessToken",
                                    body = profileRequest
                                )
                                if (!profileResponse.isSuccessful) {
                                    errorMessage.value = "Профиль не создан: ${profileResponse.code()}"
                                    return@launch
                                }
                            }
                            // Успех
                            isVerified.value = true
                        } else {
                            errorMessage.value = "Не удалось получить данные пользователя"
                        }
                    } else {
                        errorMessage.value = "Ошибка входа: ${signInResponse.code()}"
                    }
                } else {
                    errorMessage.value = "Неверный код или время истекло"
                }
            } catch (e: IOException) {
                errorMessage.value = "Отсутствует интернет-соединение"
            } catch (e: Exception) {
                errorMessage.value = "Ошибка: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun resetVerificationState() {
        isVerified.value = false
    }
}