package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.model.SignUpRequest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SignUpViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun signUp(email: String, password: String, name: String, navController: NavHostController) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                // Исправлено: authService вместо userManagementService
                val response = RetrofitInstance.authService.signUp(
                    SignUpRequest(email = email, password = password)
                )
                if (response.isSuccessful) {
                    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                    navController.navigate("verifyOTP/$email/signup?name=$encodedName")
                } else {
                    errorMessage.value = when (response.code()) {
                        400 -> "Пользователь с таким email уже существует"
                        422 -> "Некорректные данные"
                        else -> "Ошибка сервера: ${response.code()}"
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
}