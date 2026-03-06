package com.example.shstore.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.service.ProductDto
import java.io.IOException

@Composable
fun HomeScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()
    val token = UserSession.accessToken
    val userId = UserSession.userId

    val categories = listOf("Все", "Outdoor", "Tennis", "Men", "Women")
    var selectedCategory by remember { mutableStateOf("Все") }

    var allProducts by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загружаем товары с сервера (задание 7 — чтобы были реальные id для перехода в Details)
    LaunchedEffect(Unit) {
        if (token == null) return@LaunchedEffect
        isLoading = true
        try {
            allProducts = RetrofitInstance.userManagementService.getProducts()
        } catch (e: IOException) {
            errorMessage = "Нет соединения с интернетом"
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки товаров"
        } finally {
            isLoading = false
        }
    }

    val bestSellers = allProducts.filter { it.is_best_seller == true }
    val displayProducts = if (selectedCategory == "Все") allProducts else allProducts.filter {
        // фильтрация не по category_id, т.к. на главной показываем all/best sellers
        it.is_best_seller == true
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController, currentRoute = "home") },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.home_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HomeSearchBox(
                    hint = stringResource(id = R.string.search_hint),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF48B2E7))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.categories),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    HomeCategoryChip(
                        title = category,
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            if (category != "Все") {
                                navController.navigate("catalog/$category")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.popular),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(id = R.string.see_all),
                    fontSize = 14.sp,
                    color = Color(0xFF48B2E7),
                    modifier = Modifier.clickable { navController.navigate("catalog") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF48B2E7))
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(if (bestSellers.isEmpty()) allProducts.take(6) else bestSellers) { product ->
                        // Задание 7: нажатие на карточку → переход на Details
                        HomeProductCard(
                            product = product,
                            onClick = { navController.navigate("details/${product.id}") },
                            onAddToCart = { navController.navigate("cart") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.promo),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(id = R.string.see_all),
                    fontSize = 14.sp,
                    color = Color(0xFF48B2E7)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HomePromoBanner()
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}

@Composable
private fun HomeSearchBox(hint: String, modifier: Modifier = Modifier) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color(0xFFB0B0B0)
            )
        },
        placeholder = { Text(text = hint, color = Color(0xFFB0B0B0)) },
        singleLine = true,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun HomeCategoryChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF48B2E7) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = if (selected) Color.White else Color(0xFF828B99)
        )
    }
}

@Composable
fun HomeProductCard(
    product: ProductDto,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }  // Задание 7: переход на Details
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite_border),
                    contentDescription = "Favorite",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.img_shoe_blue),
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (product.is_best_seller == true) {
                Text(
                    text = "BEST SELLER",
                    fontSize = 10.sp,
                    color = Color(0xFF48B2E7),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = product.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "₽${product.cost}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF48B2E7))
                        .clickable { onAddToCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart),
                        contentDescription = "Add to cart",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePromoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_promo_banner),
            contentDescription = "Promo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ── BottomBar (общий для всех экранов) ───────────────────────────────────────

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String) {
    val activeColor = Color(0xFF48B2E7)
    val inactiveColor = Color(0xFFB0B0B0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = if (currentRoute == "home") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "home") navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Favorites",
                tint = if (currentRoute == "favorite") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "favorite") navController.navigate("favorite") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )

            // Центральная кнопка — корзина
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(activeColor)
                    .clickable {
                        if (currentRoute != "cart") navController.navigate("cart") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bag),
                    contentDescription = "Cart",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_truck),
                contentDescription = "Orders",
                tint = if (currentRoute == "orders") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "orders") navController.navigate("orders") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile",
                tint = if (currentRoute == "profile") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "profile") navController.navigate("profile") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
