package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.model.SignUpRequest
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isRegistered = mutableStateOf(false)

    fun signUp(request: SignUpRequest) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                isRegistered.value = false

                val response = RetrofitInstance.userManagementService.signUp(request)

                if (response.isSuccessful) {
                    isRegistered.value = true
                } else {
                    errorMessage.value = "Ошибка регистрации: ${response.code()}"
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

    fun resetRegistrationState() {
        isRegistered.value = false
    }
}