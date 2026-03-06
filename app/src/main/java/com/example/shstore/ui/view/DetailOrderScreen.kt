package com.example.shstore.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.OrderDto
import com.example.shstore.data.model.OrderItemDto
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailOrderScreen(navController: NavHostController, orderId: Long) {
    val token = UserSession.accessToken

    var orderItems by remember { mutableStateOf<List<OrderItemDto>>(emptyList()) }
    var orderInfo by remember { mutableStateOf<OrderDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Время заказа (из orderId навигации уже есть,
    // но подробную инфо берём с сервера)
    LaunchedEffect(orderId) {
        if (token == null) return@LaunchedEffect
        isLoading = true
        try {
            // Задание 25: загрузка позиций заказа с сервера
            orderItems = RetrofitInstance.userManagementService.getOrderItems(
                orderIdFilter = "eq.$orderId"
            )
        } catch (e: IOException) {
            // Задание 26: нет сети
            errorMessage = "Нет соединения с интернетом. Проверьте подключение."
        } catch (e: Exception) {
            // Задание 26: ошибка сервера
            errorMessage = "Ошибка сервера: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$orderId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Назад",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F7FB))
            )
        },
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->

        // Задание 27: индикатор загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF48B2E7)) }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Время создания заказа (правый верхний угол как на макете)
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            "7 мин назад",
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                // Карточки товаров
                items(orderItems) { item ->
                    DetailOrderItemCard(item = item)
                }

                // Блок контактной информации (как на Detail_Order.jpg)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Контактная информация",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email
                        DetailContactRow(
                            iconRes = R.drawable.ic_email,
                            value = UserSession.email ?: "—",
                            hint = "Email"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Телефон
                        DetailContactRow(
                            iconRes = R.drawable.ic_phone,
                            value = "+234-811-732-5298",
                            hint = "Телефон"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Адрес
                        Text("Адрес", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1082 Аэропорт, Нигерии", fontSize = 14.sp, color = Color(0xFF666666))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Карта-заглушка
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFB8D4B0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.22f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Посмотреть на карте",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF48B2E7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_location),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Способ оплаты
                        Text("Способ оплаты", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0F0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_cart),
                                    contentDescription = null,
                                    tint = Color(0xFF48B2E7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("DbL Card", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
                                Text("**** **** 0696 4629", fontSize = 12.sp, color = Color(0xFF999999))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Задание 26: диалог ошибки
    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}

// ── Карточка товара в заказе (как на Detail_Order.jpg) ───────────────────────

@Composable
private fun DetailOrderItemCard(item: OrderItemDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Изображение
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F4F8)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_shoe_blue),
                contentDescription = item.title,
                modifier = Modifier.size(68.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title ?: "Товар",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "₽${"%.2f".format(item.coast ?: 0.0)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    "${item.count ?: 1} шт",
                    fontSize = 14.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

// ── Строка контакта (только для просмотра) ────────────────────────────────────

@Composable
private fun DetailContactRow(iconRes: Int, value: String, hint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF555555),
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(value, fontSize = 14.sp, color = Color(0xFF333333))
            Text(hint, fontSize = 12.sp, color = Color(0xFF999999))
        }
    }
}
