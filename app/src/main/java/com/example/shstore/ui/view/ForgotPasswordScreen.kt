package com.example.shstore.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shstore.R
import com.example.shstore.ui.viewModel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val showDialog = viewModel.showDialog.value // Диалог "Проверьте ваш Email"

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDialog.value = false },
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF48B2E7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = R.drawable.email_icon), contentDescription = null, tint = Color.White) // Нужна иконка email
                }
            },
            title = {
                Text("Проверьте Ваш Email", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Мы Отправили Код Восстановления Пароля На Вашу Электронную Почту.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            },
            confirmButton = {
                // При нажатии переходим на OTP экран
                // Передаем тип "recovery", чтобы OTP экран знал, что мы восстанавливаем пароль
                Button(
                    onClick = {
                        viewModel.showDialog.value = false
                        navController.navigate("verifyOTP/$email/recovery")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7))
                ) {
                    Text("ОК")
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                Icon(painter = painterResource(id = R.drawable.arrow), contentDescription = null, tint = Color.Black)
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text("Забыл Пароль", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Введите Свою Учетную Запись\nДля Сброса", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("xyz@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                    focusedContainerColor = Color(0xFFF7F7F7),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    // Вызываем ViewModel для отправки письма
                    viewModel.sendRecoveryEmail(email)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7))
            ) {
                Text("Отправить")
            }
        }
    }
}
