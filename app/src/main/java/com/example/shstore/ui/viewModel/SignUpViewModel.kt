package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.model.*
import kotlinx.coroutines.launch
import java.io.IOException

class SignUpViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isRegistered = mutableStateOf(false)

    fun signUp(email: String, password: String, name: String, navController: NavHostController) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                isRegistered.value = false

                // 1. Регистрация
                val signUpResponse = RetrofitInstance.userManagementService.signUp(
                    SignUpRequest(email, password)
                )

                if (signUpResponse.isSuccessful) {
                    // 2. Вход для получения токена
                    val signInResponse = RetrofitInstance.userManagementService.signIn(
                        SignInRequest(email, password)
                    )
                    if (signInResponse.isSuccessful) {
                        val signInBody = signInResponse.body()
                        val accessToken = signInBody?.access_token
                        val userId = signInBody?.user?.id

                        if (accessToken != null && userId != null) {
                            // 3. Создание профиля
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
                            if (profileResponse.isSuccessful) {
                                isRegistered.value = true
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                errorMessage.value = "Не удалось создать профиль. Код: ${profileResponse.code()}"
                            }
                        } else {
                            errorMessage.value = "Не удалось получить данные пользователя после входа"
                        }
                    } else {
                        errorMessage.value = "Ошибка входа после регистрации: ${signInResponse.code()}"
                    }
                } else {
                    errorMessage.value = when (signUpResponse.code()) {
                        400 -> "Пользователь с таким email уже существует"
                        422 -> "Некорректные данные"
                        else -> "Ошибка сервера: ${signUpResponse.code()}"
                    }
                }
            } catch (e: IOException) {
                errorMessage.value = "Отсутствует подключение к интернету"
            } catch (e: Exception) {
                errorMessage.value = "Произошла ошибка: ${e.localizedMessage}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun resetRegistrationState() {
        isRegistered.value = false
    }
}