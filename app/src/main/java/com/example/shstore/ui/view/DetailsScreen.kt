package com.example.shstore.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.CartRequest
import com.example.shstore.data.model.FavouriteRequest
import com.example.shstore.data.service.ProductDto
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    productId: String
) {
    val token = UserSession.accessToken
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    var allProducts by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var current by remember { mutableStateOf<CatalogProduct?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId, token, userId) {
        if (token == null || userId == null) return@LaunchedEffect
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService
            val products: List<ProductDto> = service.getProducts()
            val favs = service.getFavourites(
                userIdFilter = "eq.$userId"
            )
            val favSet = favs.mapNotNull { it.product_id }.toSet()

            val mapped = products.map { p ->
                CatalogProduct(
                    id = p.id,
                    title = p.title,
                    price = p.cost,
                    categoryId = p.category_id,
                    isBestSeller = p.is_best_seller == true,
                    imageRes = R.drawable.img_shoe_blue,
                    isFavorite = favSet.contains(p.id),
                    description = p.description
                )
            }
            allProducts = mapped
            current = mapped.firstOrNull { it.id == productId } ?: mapped.firstOrNull()
        } catch (e: java.io.IOException) {
            // Отсутствует соединение с интернетом
            errorMessage = "Нет соединения с интернетом"
        } catch (e: Exception) {
            // Ошибка сервера или парсинга
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun toggleFavourite(product: CatalogProduct, isFav: Boolean) {
        if (token == null || userId == null) return
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService

                if (isFav) {
                    service.addFavourite(
                        body = FavouriteRequest(
                            user_id = userId,
                            product_id = product.id
                        )
                    )
                } else {
                    service.deleteFavourite(
                        userIdFilter = "eq.$userId",
                        productIdFilter = "eq.${product.id}"
                    )
                }
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
                current = current?.let {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
            } catch (_: Exception) { }
        }
    }

    val product = current

    // Диалог ошибки
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Ошибка") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->
        if (isLoading || product == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF48B2E7))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FB))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Назад"
                        )
                    }
                    Text(
                        text = "Sneaker Shop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { toggleFavourite(product, !product.isFavorite) }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isFavorite)
                                    R.drawable.ic_heart_filled
                                else
                                    R.drawable.ic_favorite_border
                            ),
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Men's Shoes",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₽${product.price}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = product.imageRes),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Горизонтальный скролл с миниатюрами товаров (задание 10)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        allProducts.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (p.id == product.id)
                                            Color(0xFFE2F3FF)
                                        else
                                            Color(0xFFF2F4F7)
                                    )
                                    .clickable { current = p },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = p.imageRes),
                                    contentDescription = p.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Описание",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Описание товара из базы данных
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { toggleFavourite(product, !product.isFavorite) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isFavorite)
                                    R.drawable.ic_heart_filled
                                else
                                    R.drawable.ic_favorite_border
                            ),
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0)
                        )
                    }

                    Button(
                        onClick = {
                            if (userId == null) return@Button

                            scope.launch {
                                try {
                                    RetrofitInstance.userManagementService.addToCart(
                                        CartRequest(
                                            user_id = userId,
                                            product_id = product.id,
                                            count = 1
                                        )
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF48B2E7)
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "В корзину",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}