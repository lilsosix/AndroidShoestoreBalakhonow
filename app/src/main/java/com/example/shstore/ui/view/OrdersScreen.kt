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
import com.example.shstore.data.model.CartRequest
import com.example.shstore.data.model.OrderDto
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// ── Группировка ───────────────────────────────────────────────────────────────

private sealed class OrderListItem {
    data class Header(val label: String) : OrderListItem()
    data class Row(val order: OrderDto) : OrderListItem()
}

private val MONTHS_RU = arrayOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
)

private fun parseOrderDate(isoStr: String?): Date? {
    if (isoStr == null) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(isoStr)
    } catch (_: Exception) {
        try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(isoStr) }
        catch (_: Exception) { null }
    }
}

private fun sameDay(date: Date, cal: Calendar): Boolean {
    val c = Calendar.getInstance().apply { time = date }
    return c.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            c.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
}

private fun minsAgo(date: Date) = (System.currentTimeMillis() - date.time) / 60_000

private fun timeLabel(date: Date): String {
    val today = Calendar.getInstance()
    return if (sameDay(date, today)) {
        val m = minsAgo(date)
        if (m < 60) "$m мин назад" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } else SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}

private fun groupOrderList(orders: List<OrderDto>): List<OrderListItem> {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
    val result = mutableListOf<OrderListItem>()
    var lastHeader = ""
    orders.forEach { order ->
        val date = parseOrderDate(order.created_at)
        val header = if (date == null) "Ранее" else when {
            sameDay(date, today)     -> "Недавний"
            sameDay(date, yesterday) -> "Вчера"
            else -> {
                val cal = Calendar.getInstance().apply { time = date }
                "${cal.get(Calendar.DAY_OF_MONTH)} ${MONTHS_RU[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
            }
        }
        if (header != lastHeader) { result.add(OrderListItem.Header(header)); lastHeader = header }
        result.add(OrderListItem.Row(order))
    }
    return result
}

// ── Экран ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavHostController) {
    val token = UserSession.accessToken
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadOrders() {
        if (token == null || userId == null) return
        isLoading = true
        scope.launch {
            try {
                orders = RetrofitInstance.userManagementService.getOrders(
                    userIdFilter = "eq.$userId"
                )
            } catch (e: IOException) {
                errorMessage = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadOrders() }

    fun cancelOrder(order: OrderDto) {
        orders = orders.filter { it.id != order.id }
        scope.launch {
            try {
                RetrofitInstance.userManagementService.updateOrderStatus(
                    idFilter = "eq.${order.id}",
                    body = mapOf("status_id" to null)
                )
            } catch (_: Exception) {
                orders = orders + order
                errorMessage = "Не удалось отменить заказ"
            }
        }
    }

    fun repeatOrder(order: OrderDto) {
        if (token == null || userId == null) return
        scope.launch {
            try {
                val items = RetrofitInstance.userManagementService.getOrderItems(
                    orderIdFilter = "eq.${order.id}"
                )
                items.forEach { item ->
                    item.product_id?.let { productId ->
                        RetrofitInstance.userManagementService.addToCart(
                            CartRequest(
                                product_id = productId,
                                user_id = userId,
                                count = 1
                            )
                        )
                    }
                }
                navController.navigate("cart")
            } catch (_: Exception) {
                errorMessage = "Не удалось повторить заказ"
            }
        }
    }

    val grouped = remember(orders) { groupOrderList(orders) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказы", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F7FB))
            )
        },
        bottomBar = { BottomBar(navController = navController, currentRoute = "orders") },
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF48B2E7)) }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Заказов пока нет", color = Color(0xFF9E9E9E), fontSize = 16.sp) }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grouped, key = {
                    when (it) {
                        is OrderListItem.Header -> "h_${it.label}"
                        is OrderListItem.Row    -> "r_${it.order.id}"
                    }
                }) { item ->
                    when (item) {
                        is OrderListItem.Header -> {
                            Text(
                                item.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        is OrderListItem.Row -> {
                            OrderSwipeRow(
                                onRepeat = { repeatOrder(item.order) },
                                onCancel = { cancelOrder(item.order) }
                            ) {
                                OrderRowCard(
                                    order = item.order,
                                    onClick = { navController.navigate("detail_order/${item.order.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}

// ── Карточка заказа (как на Orders.png) ──────────────────────────────────────

@Composable
private fun OrderRowCard(order: OrderDto, onClick: () -> Unit) {
    val date = parseOrderDate(order.created_at)
    val label = date?.let { timeLabel(it) } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Изображение товара
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F4F8)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_shoe_blue),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "№ ${order.id}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF48B2E7)
                )
                Text(
                    label,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                order.order_status?.name ?: "Nike Air Max",
                fontSize = 13.sp,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "₽${"%.2f".format((order.delivery_coast ?: 0L).toDouble())}",
                    fontSize = 13.sp,
                    color = Color(0xFF333333)
                )
                order.delivery_coast?.let { coast ->
                    if (coast > 0) {
                        Text(
                            "₽${"%.2f".format(coast.toDouble())}",
                            fontSize = 13.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

// ── Свайпабельная строка заказа ───────────────────────────────────────────────
// Свайп ВПРАВО → синяя кнопка обновить (повторить)
// Свайп ВЛЕВО  → красная кнопка отменить

@Composable
private fun OrderSwipeRow(
    onRepeat: () -> Unit,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val threshold = 170f

    Box(modifier = Modifier.fillMaxWidth()) {

        // Левая кнопка — повторить (правый свайп)
        if (offsetX.value > 20f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF48B2E7))
                    .clickable { onRepeat() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = "Повторить",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Правая кнопка — отменить (левый свайп)
        if (offsetX.value < -20f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEF5959))
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Отменить",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > threshold / 2  -> offsetX.animateTo(threshold)
                                    offsetX.value < -threshold / 2 -> offsetX.animateTo(-threshold)
                                    else                           -> offsetX.animateTo(0f)
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-threshold, threshold)
                                )
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
