package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.ChangePasswordRequest
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
                // Валидация
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

                // Получаем токен из сессии
                val accessToken = UserSession.accessToken
                if (accessToken == null) {
                    errorMessage.value = "Ошибка авторизации. Токен не найден"
                    return@launch
                }

                // Создаем запрос на смену пароля с email и новым паролем
                val changePasswordRequest = ChangePasswordRequest(
                    email = email,
                    newPassword = password.value
                )

                // Формируем заголовок авторизации
                val authHeader = "Bearer $accessToken"

                // Вызываем метод с правильными параметрами
                val response = RetrofitInstance.userManagementService.changePassword(
                    authHeader = authHeader,
                    request = changePasswordRequest
                )

                if (response.isSuccessful) {
                    // Пароль успешно сменён – очищаем сессию и отправляем на экран входа
                    UserSession.userId = null
                    UserSession.accessToken = null

                    navController.navigate("sign_in") {
                        popUpTo("new_password") { inclusive = true }
                    }
                } else {
                    errorMessage.value = "Ошибка: ${response.code()} - ${response.message()}"
                }
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