package com.example.shstore.ui.view

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shstore.ui.theme.ShStoreTheme
import com.example.shstore.ui.viewModel.SignInViewModel
import com.example.shstore.data.model.SignInRequest
import com.example.shstore.ui.viewModel.SignInUiState

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SignInViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignInUiState.Error -> {
                errorMessage = (uiState as SignInUiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Ошибка") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F2F2))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {

                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Привет!",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Заполните Свои Данные",
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Email",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                StyledTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "xyz@gmail.com",
                    keyboardType = KeyboardType.Email,
                    enabled = uiState !is SignInUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Пароль",
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
                                imageVector = if (showPassword)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = "Показать/скрыть пароль",
                                tint = Color(0xFFB0B0B0)
                            )
                        }
                    },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    enabled = uiState !is SignInUiState.Loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Восстановить",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            if (uiState !is SignInUiState.Loading) {
                                navController.navigate("forgot_password")
                            }
                        }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        when {
                            email.isBlank() || password.isBlank() -> {
                                errorMessage = "Заполните все поля"
                                showErrorDialog = true
                            }
                            else -> {
                                viewModel.signIn(
                                    SignInRequest(email.trim(), password.trim()),
                                    navController = navController
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF48B2E7),
                        contentColor = Color.White
                    ),
                    enabled = uiState !is SignInUiState.Loading
                ) {
                    if (uiState is SignInUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Войти",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Вы впервые?",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = " Создать",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.clickable {
                            if (uiState !is SignInUiState.Loading) {
                                navController.navigate("register")
                            }
                        }
                    )
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = Color(0xFFCBCBCB)
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF333333),
            unfocusedTextColor = Color(0xFF333333),
            disabledTextColor = Color(0xFF999999),
            focusedContainerColor = Color(0xFFF7F7F7),
            unfocusedContainerColor = Color(0xFFF7F7F7),
            disabledContainerColor = Color(0xFFF7F7F7),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = Color(0xFF333333),
            focusedPlaceholderColor = Color(0xFFCBCBCB),
            unfocusedPlaceholderColor = Color(0xFFCBCBCB)
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ShStoreTheme {
        val navController = rememberNavController()
        LoginScreen(navController = navController)
    }
}