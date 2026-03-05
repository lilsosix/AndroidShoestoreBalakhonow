package com.example.shstore.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shstore.ui.theme.ShStoreTheme
import com.example.shstore.ui.viewModel.VerifyOTPViewModel

@Composable
fun VerifyOTPScreen(
    navController: NavHostController,
    email: String,
    otpType: String = "signup", // "signup" или "recovery"
    viewModel: VerifyOTPViewModel = viewModel()
) {
    var otpValue by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    val otpLength = 8 // Изменено с 6 на 8

    LaunchedEffect(otpValue.text) {
        if (otpValue.text.length == otpLength) {
            viewModel.verifyOTP(email, otpValue.text, otpType, context, navController)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F7F7))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.example.shstore.R.drawable.arrow),
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

            OtpInputField(
                otpValue = otpValue,
                onValueChange = {
                    if (it.text.length <= otpLength) {
                        otpValue = it
                    }
                },
                length = otpLength
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    length: Int
) {
    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = otpValue,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp), // Добавил отступы между ячейками
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(length) { index ->
                        val char = if (index < otpValue.text.length) otpValue.text[index] else null
                        val isFocused = index == otpValue.text.length

                        OtpCell(char = char, isFocused = isFocused)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent)
        )
    }
}

@Composable
fun OtpCell(
    char: Char?,
    isFocused: Boolean
) {
    val borderColor = if (isFocused) Color(0xFFFF5252) else Color(0xFFF7F7F7)
    val backgroundColor = Color(0xFFF7F7F7)

    Box(
        modifier = Modifier
            .width(38.dp) // Уменьшил ширину с 48dp до 40dp, чтобы 8 ячеек поместились на экране
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.dp else 0.dp, // Увеличил толщину границы для лучшей видимости
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyOTPScreenPreview() {
    ShStoreTheme {
        val navController = rememberNavController()
        VerifyOTPScreen(
            navController = navController,
            email = "test@example.com",
            otpType = "recovery"
        )
    }
}