package com.example.shstore.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(navController: NavHostController) {
    val token = UserSession.accessToken
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // загрузка избранного
    LaunchedEffect(token, userId) {
        if (token == null || userId == null) return@LaunchedEffect
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService

            val favs = service.getFavourites(
                userIdFilter = "eq.$userId",
            )

            if (favs.isEmpty()) {
                products = emptyList()
            } else {
                val allProducts = service.getProducts(
                )
                val favIds = favs.mapNotNull { it.product_id }.toSet()
                products = allProducts
                    .filter { favIds.contains(it.id) }
                    .map { p ->
                        CatalogProduct(
                            id = p.id,
                            title = p.title,
                            price = p.cost,
                            categoryId = p.category_id,
                            isBestSeller = p.is_best_seller == true,
                            imageRes = R.drawable.img_shoe_blue,
                            isFavorite = true
                        )
                    }
            }
        } finally {
            isLoading = false
        }
    }

    // снять из избранного прямо на этом экране
    fun removeFromFavourite(product: CatalogProduct) {
        if (token == null || userId == null) return
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService
                service.deleteFavourite(
                    userIdFilter = "eq.$userId",
                    productIdFilter = "eq.${product.id}"
                )
                products = products.filter { it.id != product.id }
            } catch (_: Exception) {
                // можно добавить лог при желании
            }
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController, currentRoute = "favorite") },
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->
        // убираем верхний отступ, оставляем только снизу, чтобы всё было выше
        val contentPadding = PaddingValues(
            start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding()
        )

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Избранное",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_filled),
                        contentDescription = "Favorite",
                        tint = Color(0xFFDD4B4B),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(20.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F7FB)
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF48B2E7))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        FavoriteProductCard(
                            product = product,
                            onRemove = { removeFromFavourite(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteProductCard(
    product: CatalogProduct,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_filled),
                        contentDescription = "Favorite",
                        tint = Color(0xFFDD4B4B),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onRemove() } // клик по сердечку убирает из избранного
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F4F7)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (product.isBestSeller) {
                Text(
                    text = "BEST SELLER",
                    fontSize = 10.sp,
                    color = Color(0xFF48B2E7),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = product.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₽${product.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF48B2E7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
