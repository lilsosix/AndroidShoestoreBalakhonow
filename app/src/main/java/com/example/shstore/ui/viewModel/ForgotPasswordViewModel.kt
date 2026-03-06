package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.service.RecoverRequest
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    val showDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun sendRecoveryEmail(email: String) {
        viewModelScope.launch {
            try {
                errorMessage.value = null

                // Исправлено: authService вместо userManagementService
                val response = RetrofitInstance.authService
                    .recoverPassword(RecoverRequest(email = email))

                if (response.isSuccessful) {
                    showDialog.value = true
                } else {
                    errorMessage.value = "Ошибка: ${response.code()} ${response.message()}"
                }
            } catch (e: java.io.IOException) {
                errorMessage.value = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }
}