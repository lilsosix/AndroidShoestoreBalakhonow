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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shstore.ui.theme.ShStoreTheme
import com.example.shstore.ui.viewModel.SignUpViewModel

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

    // Используем .verticalScroll(rememberScrollState()) в модификаторе Column,
    // но с fillMaxHeight() для правильной работы веса (weight)
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isFormValid = name.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 6 &&
            isTermsAccepted

    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        // Чтобы weight(1f) работал внутри скролла, используем fillMaxSize()
        // и verticalScroll().
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

            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Регистрация", fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text("Заполните Свои Данные", fontSize = 14.sp, color = Color(0xFFB0B0B0), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(40.dp))

            // Поля ввода
            Text("Ваше имя", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(value = name, onValueChange = { name = it }, placeholder = "Иван Иванов")

            Spacer(modifier = Modifier.height(16.dp))
            Text("Email", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(value = email, onValueChange = { email = it }, placeholder = "xyz@gmail.com", keyboardType = KeyboardType.Email)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Пароль", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "********",
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color(0xFFB0B0B0))
                    }
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Чекбокс
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldCheckbox(checked = isTermsAccepted, onCheckedChange = { isTermsAccepted = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Даю согласие на обработку персональных данных", fontSize = 13.sp, color = Color(0xFF4A4A4A))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Кнопка
            Button(
                onClick = {
                    viewModel.signUp(email.trim(), password.trim(), navController)
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
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Зарегистрироваться", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Есть аккаунт? ", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                Text("Войти", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), modifier = Modifier.clickable { navController.navigate("login") })
            }
        }
    }
}

// ОСТАВЬТЕ StyledTextField и ShieldCheckbox ТАКИМИ ЖЕ, КАК БЫЛИ
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
