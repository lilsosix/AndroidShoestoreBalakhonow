package com.example.shstore.ui.view

/**
 * Экран верификации OTP-кода (одноразового пароля).
 * Supabase присылает 6-значный числовой код на email.
 * Реализована: кнопка подтверждения, таймер 01:00 для повторного запроса,
 * красная подсветка ячеек при неверном коде.
 *
 * Дата создания: 2025
 * Автор: ShStore Team
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.shstore.ui.viewModel.VerifyOTPViewModel
import kotlinx.coroutines.delay

@Composable
fun VerifyOTPScreen(
    navController: NavHostController,
    email: String,
    otpType: String = "signup",
    name: String = "",
    password: String = "",
    viewModel: VerifyOTPViewModel = viewModel()
) {
    var otpValue by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    /** Supabase присылает 6-значный код. Ранее стояло 8 — код никогда не отправлялся автоматически */
    val otpLength = 6

    /** true = ячейки подсвечиваются красным при ошибке верификации */
    var hasError by remember { mutableStateOf(false) }

    /** Таймер обратного отсчёта для повторного запроса кода (задание 24) */
    var timerSeconds by remember { mutableStateOf(60) }
    var timerRunning by remember { mutableStateOf(true) }

    // Запускаем таймер обратного отсчёта 01:00
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            timerSeconds = 60
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            timerRunning = false
        }
    }

    // Следим за ошибкой: если появилась — подсвечиваем ячейки красным (задание 25)
    LaunchedEffect(viewModel.errorMessage.value) {
        hasError = viewModel.errorMessage.value != null
    }

    // Следим за успешной верификацией
    LaunchedEffect(viewModel.isVerified.value) {
        if (viewModel.isVerified.value) {
            viewModel.resetVerificationState()
            navController.navigate("home") {
                popUpTo("onboard1") { inclusive = true }
            }
        }
    }

    /** Отправить код на сервер (задание 26) */
    fun submitOtp() {
        if (otpValue.text.length != otpLength) return
        hasError = false
        viewModel.verifyOTP(
            email = email,
            otp = otpValue.text,
            type = otpType,
            name = name,
            password = password,
            context = context,
            navController = navController
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Кнопка Назад
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F7F7))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF555555)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "OTP Проверка",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Пожалуйста, Проверьте Свою\nЭлектронную Почту, Чтобы Увидеть Код\nПодтверждения",
                fontSize = 14.sp,
                color = Color(0xFF7D7D7D),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "OTP Код",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Поле ввода OTP — 6 ячеек, красные при ошибке (задание 25)
            OtpInputField(
                otpValue = otpValue,
                onValueChange = {
                    if (it.text.length <= otpLength) {
                        otpValue = it
                        // Сбрасываем ошибку при изменении
                        if (hasError) {
                            hasError = false
                            viewModel.clearError()
                        }
                    }
                },
                length = otpLength,
                hasError = hasError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Таймер и кнопка повторного запроса кода (задание 24)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timerRunning) {
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    Text(
                        text = "Повторить через %02d:%02d".format(minutes, seconds),
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    Text(
                        text = "Отправить код повторно",
                        fontSize = 14.sp,
                        color = Color(0xFF48B2E7),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            // Повторный запрос — перезапуск таймера
                            timerRunning = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка подтверждения кода (главная причина, почему ничего не происходило)
            Button(
                onClick = { submitOtp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7)),
                enabled = otpValue.text.length == otpLength && !viewModel.isLoading.value
            ) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Подтвердить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Диалог ошибки верификации
    if (viewModel.errorMessage.value != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
                hasError = false
            },
            title = { Text("Ошибка") },
            text = { Text(viewModel.errorMessage.value ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearError()
                    hasError = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * Поле ввода OTP из нескольких ячеек.
 * hasError — если true, все ячейки подсвечиваются красным (задание 25).
 */
@Composable
fun OtpInputField(
    otpValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    length: Int,
    hasError: Boolean = false
) {
    Box(contentAlignment = Alignment.CenterStart) {
        BasicTextField(
            value = otpValue,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(length) { index ->
                        val char = if (index < otpValue.text.length) otpValue.text[index] else null
                        val isFocused = index == otpValue.text.length
                        OtpCell(char = char, isFocused = isFocused, hasError = hasError)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent)
        )
    }
}

/**
 * Одна ячейка OTP-кода.
 * hasError — красная рамка у всех ячеек (задание 25).
 * isFocused — синяя рамка у текущей активной ячейки.
 */
@Composable
fun OtpCell(
    char: Char?,
    isFocused: Boolean,
    hasError: Boolean = false
) {
    val borderColor = when {
        hasError -> Color(0xFFFF5252)          // Красный — неверный код (задание 25)
        isFocused -> Color(0xFF48B2E7)          // Синий — активная ячейка
        else -> Color.Transparent
    }
    val borderWidth = if (hasError || isFocused) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F7F7))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}
