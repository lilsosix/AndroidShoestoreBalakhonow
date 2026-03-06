package com.example.shstore.ui.viewModel

/**
 * ViewModel для экрана входа (Sign In).
 * Управляет состоянием авторизации пользователя через Supabase Auth API.
 * Использует StateFlow и sealed class SignInUiState для реактивного UI.
 *
 * Дата создания: 2025
 * Автор: ShStore Team
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.SignInRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Состояния UI экрана Sign In.
 * Idle — начальное состояние, Loading — идёт запрос,
 * Success — вход выполнен, Error — ошибка с текстом.
 */
sealed class SignInUiState {
    object Idle : SignInUiState()
    object Loading : SignInUiState()
    object Success : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}

class SignInViewModel : ViewModel() {

    /** Текущее состояние UI, наблюдаемое экраном через collectAsState() */
    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    /**
     * Выполняет авторизацию пользователя по email и паролю.
     * При успехе сохраняет токен и userId в UserSession,
     * затем выполняет навигацию на экран home.
     *
     * @param signInRequest данные для входа (email, password)
     * @param navController контроллер навигации для перехода после входа
     */
    fun signIn(signInRequest: SignInRequest, navController: NavController) {
        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            try {
                val response = RetrofitInstance.authService.signIn(signInRequest)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        UserSession.accessToken = body.access_token
                        UserSession.userId = body.user?.id
                        UserSession.email = signInRequest.email
                    }
                    _uiState.value = SignInUiState.Success
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Неверный email или пароль"
                        422 -> "Некорректные данные"
                        else -> "Ошибка сервера: ${response.code()}"
                    }
                    _uiState.value = SignInUiState.Error(errorMsg)
                }
            } catch (e: java.io.IOException) {
                _uiState.value = SignInUiState.Error("Нет соединения с интернетом")
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    /**
     * Сбрасывает состояние UI в Idle.
     * Вызывается после закрытия диалога с ошибкой.
     */
    fun resetState() {
        _uiState.value = SignInUiState.Idle
    }
}
