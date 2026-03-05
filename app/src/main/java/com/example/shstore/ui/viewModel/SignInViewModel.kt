package com.example.shstore.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.SignInRequest
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    var showDialog = mutableStateOf(false)
    var dialogText = mutableStateOf("")

    fun signIn(signInRequest: SignInRequest, navController: NavController) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.userManagementService.signIn(signInRequest)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // предполагаем, что в SignInResponse есть поля access_token и user.id
                        val accessToken = body.access_token
                        val userId = body.user.id

                        UserSession.accessToken = accessToken
                        UserSession.userId = userId
                    }

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    dialogText.value = "Неверный логин или пароль"
                    showDialog.value = true
                }
            } catch (e: Exception) {
                dialogText.value = "Ошибка: ${e.message}"
                showDialog.value = true
            }
        }
    }
}
