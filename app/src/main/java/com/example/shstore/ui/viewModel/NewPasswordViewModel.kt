package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.service.UpdatePasswordRequest
import kotlinx.coroutines.launch

class NewPasswordViewModel : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val passwordError = mutableStateOf<String?>(null)

    fun updatePassword(newPassword: String) {
        password.value = newPassword
        validatePasswords()
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword.value = newConfirmPassword
        validatePasswords()
    }

    private fun validatePasswords() {
        passwordError.value = when {
            password.value != confirmPassword.value -> "Пароли не совпадают"
            password.value.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
        }
    }

    fun changePassword(email: String, navController: NavController) {
        viewModelScope.launch {
            try {
                if (password.value != confirmPassword.value) {
                    errorMessage.value = "Пароли не совпадают"
                    return@launch
                }
                if (password.value.length < 6) {
                    errorMessage.value = "Пароль должен содержать минимум 6 символов"
                    return@launch
                }

                isLoading.value = true
                errorMessage.value = null

                val accessToken = UserSession.accessToken
                if (accessToken == null) {
                    errorMessage.value = "Ошибка авторизации. Токен не найден"
                    return@launch
                }

                // Исправлено: authService.updatePassword вместо userManagementService.changePassword
                val response = RetrofitInstance.authService.updatePassword(
                    authHeader = "Bearer $accessToken",
                    body = UpdatePasswordRequest(password = password.value)
                )

                if (response.isSuccessful) {
                    UserSession.userId = null
                    UserSession.accessToken = null
                    navController.navigate("login") {
                        popUpTo("new_password") { inclusive = true }
                    }
                } else {
                    errorMessage.value = "Ошибка: ${response.code()} - ${response.message()}"
                }
            } catch (e: java.io.IOException) {
                errorMessage.value = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}