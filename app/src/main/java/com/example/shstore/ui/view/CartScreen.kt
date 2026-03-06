package com.example.shstore.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.CartItemDto
import com.example.shstore.data.service.UpdateCountRequest
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.roundToInt

data class CartUiItem(
    val cartId: String,
    val productId: String?,
    val title: String,
    val price: Double,
    val count: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavHostController) {
    val token = UserSession.accessToken
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<CartUiItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadCart() {
        if (token == null || userId == null) return
        isLoading = true
        scope.launch {
            try {
                val raw: List<CartItemDto> = RetrofitInstance.userManagementService.getCartItems(
                    userIdFilter = "eq.$userId"
                )
                items = raw.map { dto ->
                    CartUiItem(
                        cartId = dto.id,
                        productId = dto.product_id,
                        title = dto.products?.title ?: "Товар",
                        price = dto.products?.cost ?: 0.0,
                        count = dto.count ?: 1L
                    )
                }
            } catch (e: IOException) {
                errorMessage = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadCart() }

    fun deleteItem(item: CartUiItem) {
        val prev = items
        items = items.filter { it.cartId != item.cartId }
        scope.launch {
            try {
                RetrofitInstance.userManagementService.removeCartItem(
                    idFilter = "eq.${item.cartId}"
                )
            } catch (_: Exception) {
                items = prev
                errorMessage = "Не удалось удалить товар"
            }
        }
    }

    fun updateCount(item: CartUiItem, newCount: Long) {
        if (newCount < 1) { deleteItem(item); return }
        val prev = items
        items = items.map { if (it.cartId == item.cartId) it.copy(count = newCount) else it }
        scope.launch {
            try {
                // Исправлено: используем UpdateCountRequest вместо Map
                RetrofitInstance.userManagementService.updateCartItemCount(
                    idFilter = "eq.${item.cartId}",
                    body = UpdateCountRequest(count = newCount)
                )
            } catch (_: Exception) {
                items = prev
                errorMessage = "Не удалось обновить количество"
            }
        }
    }

    val subtotal = items.sumOf { it.price * it.count }
    val delivery = if (items.isNotEmpty()) 60.20 else 0.0
    val total = subtotal + delivery

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFF48B2E7)) }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { Text("Корзина пуста", color = Color(0xFF9E9E9E), fontSize = 16.sp) }
            } else {
                Text(
                    text = "${items.size} ${pluralItems(items.size)}",
                    fontSize = 14.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items, key = { it.cartId }) { item ->
                        CartSwipeRow(
                            item = item,
                            onIncrease = { updateCount(item, item.count + 1) },
                            onDecrease = { updateCount(item, item.count - 1) },
                            onDelete = { deleteItem(item) }
                        )
                    }
                }
            }

            if (items.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FB))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Сумма", fontSize = 14.sp, color = Color(0xFF888888))
                        Text("₽${"%.2f".format(subtotal)}", fontSize = 14.sp, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Доставка", fontSize = 14.sp, color = Color(0xFF888888))
                        Text("₽${"%.2f".format(delivery)}", fontSize = 14.sp, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Итого", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
                        Text(
                            "₽${"%.2f".format(total)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF48B2E7)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("checkout") },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7))
                    ) {
                        Text(
                            "Оформить Заказ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}

@Composable
private fun CartSwipeRow(
    item: CartUiItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val rightThreshold = 200f
    val leftThreshold = 180f

    Box(modifier = Modifier.fillMaxWidth()) {
        if (offsetX.value > 20f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(72.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFF48B2E7)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp)
                ) {
                    Text("+", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onIncrease() })
                    Text("${item.count}", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("−", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDecrease() })
                }
            }
        }

        if (offsetX.value < -20f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(72.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(Color(0xFFEF5959))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Удалить",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > rightThreshold / 2 -> offsetX.animateTo(rightThreshold)
                                    offsetX.value < -leftThreshold / 2 -> offsetX.animateTo(-leftThreshold)
                                    else -> offsetX.animateTo(0f)
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-leftThreshold, rightThreshold)
                                )
                            }
                        }
                    )
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_shoe_blue),
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(4.dp))
                Text("₽${"%.2f".format(item.price)}", fontSize = 13.sp, color = Color(0xFF666666))
            }
        }
    }
}

@Composable
fun DashedDivider() {
    androidx.compose.foundation.Canvas(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(1.dp)
    ) {
        val dashWidth = 10.dp.toPx()
        val gapWidth = 6.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = Color(0xFFDDDDDD),
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x + dashWidth, 0f),
                strokeWidth = 2.dp.toPx()
            )
            x += dashWidth + gapWidth
        }
    }
}

private fun pluralItems(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "товар"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "товара"
    else -> "товаров"
}