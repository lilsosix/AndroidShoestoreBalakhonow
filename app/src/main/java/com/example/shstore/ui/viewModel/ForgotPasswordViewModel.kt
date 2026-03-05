package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shstore.data.RetrofitInstance
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    val showDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun sendRecoveryEmail(email: String) {
        viewModelScope.launch {
            try {
                errorMessage.value = null

                val response = RetrofitInstance.userManagementService
                    .recoverPassword(mapOf("email" to email))

                if (response.isSuccessful) {
                    // Письмо отправлено – показываем диалог "Проверьте email"
                    showDialog.value = true
                } else {
                    errorMessage.value = "Ошибка: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }
}
