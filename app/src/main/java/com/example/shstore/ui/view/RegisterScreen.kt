package com.example.shstore.ui.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shstore.R
import com.example.shstore.ui.theme.ShStoreTheme
import com.example.shstore.ui.viewModel.SignUpViewModel
import com.example.shstore.data.model.SignUpRequest

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SignUpViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-z0-9]+@[a-z0-9]+\\.[a-z]{3,}$".toRegex()
        return emailRegex.matches(email)
    }

    val isFormValid = name.isNotBlank() &&
            isValidEmail(email) &&
            password.length >= 6 &&
            isTermsAccepted

    // Реакция на успешную регистрацию
    LaunchedEffect(viewModel.isRegistered.value) {
        if (viewModel.isRegistered.value) {
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
            viewModel.resetRegistrationState()
        }
    }

    // Реакция на ошибки
    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { msg ->
            errorDialogMessage = msg
            showErrorDialog = true
            viewModel.clearError()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Кнопка "Назад"
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F2))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Назад",
                    tint = Color(0xFF555555)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                "Регистрация",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Заполните Свои Данные",
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Имя
            Text(
                "Ваше имя",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Иван Иванов"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            Text(
                "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = email,
                onValueChange = { newValue ->
                    email = newValue.lowercase()
                },
                placeholder = "xyz@gmail.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Пароль
            Text(
                "Пароль",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "********",
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль",
                            tint = Color(0xFF48B2E7)
                        )
                    }
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Чекбокс согласия
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldCheckbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Даю согласие на обработку персональных данных",
                    fontSize = 13.sp,
                    color = Color(0xFF4A4A4A)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Кнопка регистрации
            Button(
                onClick = {
                    if (!isValidEmail(email)) {
                        errorDialogMessage = "Email должен соответствовать формату:\n" +
                                "только маленькие буквы и цифры до @,\n" +
                                "после @ — только маленькие буквы и цифры,\n" +
                                "домен верхнего уровня — минимум 3 буквы"
                        showErrorDialog = true
                    } else {
                        viewModel.signUp(
                            SignUpRequest(
                                email = email.trim(),
                                password = password.trim(),
                                name = name.trim()
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF48B2E7),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B6B8B),
                    disabledContentColor = Color.White
                ),
                enabled = isFormValid && !viewModel.isLoading.value
            ) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Зарегистрироваться", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Ссылка на вход
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Есть аккаунт? ",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    "Войти",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
        }
    }

    // Диалог ошибок
    if (showErrorDialog) {
        Dialog(onDismissRequest = { showErrorDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.shield),
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ошибка",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorDialogMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showErrorDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7))
                    ) {
                        Text("Понятно", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = 14.sp, color = Color(0xFFCBCBCB)) },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7F7F7),
            unfocusedContainerColor = Color(0xFFF7F7F7),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun ShieldCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val backgroundColor = if (checked) Color(0xFF48B2E7) else Color(0xFFF2F2F2)
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    ShStoreTheme {
        val navController = rememberNavController()
        RegisterScreen(navController = navController)
    }
}