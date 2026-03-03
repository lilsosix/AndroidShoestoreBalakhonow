package com.example.shstore.ui.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.model.VerifyOtpRequest
import kotlinx.coroutines.launch

class VerifyOTPViewModel : ViewModel() {

    // type: "signup" или "recovery"
    fun verifyOTP(
        email: String,
        token: String,
        type: String,
        context: Context,
        navController: NavController
    ) {
        viewModelScope.launch {
            try {
                val requestType = if (type == "recovery") "recovery" else "signup"

                val request = VerifyOtpRequest(
                    type = requestType,
                    email = email,
                    token = token
                )

                val response = RetrofitInstance.userManagementService.verifyOTP(request)

                if (response.isSuccessful) {
                    if (type == "recovery") {
                        // После успешного ввода кода для восстановления → экран нового пароля
                        navController.navigate("new_password/$email")
                    } else {
                        // После подтверждения регистрации → на логин
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                } else {
                    Toast.makeText(context, "Неверный код", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
