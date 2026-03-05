package com.example.shstore.ui.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.FavouriteRequest
import com.example.shstore.data.service.ProductDto
import kotlinx.coroutines.launch
import java.io.IOException

data class CatalogCategory(
    val id: String,
    val title: String
)

data class CatalogProduct(
    val id: String,
    val title: String,
    val price: Double,
    val categoryId: String?,
    val isBestSeller: Boolean,
    val imageRes: Int,
    val isFavorite: Boolean = false,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavHostController,
    initialCategoryTitle: String = "Outdoor"
) {
    val categories = listOf(
        CatalogCategory("all", "Все"),
        CatalogCategory("ea4ed603-8cbe-4d57-a359-b6b843a645bc", "Outdoor"),
        CatalogCategory("4f3a690b-41bf-4fca-8ffc-67cc385c6637", "Tennis"),
        CatalogCategory("76ab9d74-7d5b-4dee-9c67-6ed4019fa202", "Men"),
        CatalogCategory("8143b506-d70a-41ec-a5eb-3cf09627da9e", "Women")
    )

    val sessionUserId = UserSession.userId
    val token = UserSession.accessToken
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var allProducts by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(initialCategoryTitle) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загрузка данных с сервера
    fun loadData() {
        if (token == null || sessionUserId == null) {
            errorMessage = "Пользователь не авторизован"
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService
                val products: List<ProductDto> = service.getProducts(
                    authHeader = "Bearer $token"
                )
                val favs = service.getFavourites(
                    authHeader = "Bearer $token",
                    userIdFilter = "eq.$sessionUserId"
                )
                val favSet = favs.mapNotNull { it.product_id }.toSet()

                allProducts = products.map { p ->
                    CatalogProduct(
                        id = p.id,
                        title = p.title,
                        price = p.cost,
                        categoryId = p.category_id,
                        isBestSeller = p.is_best_seller == true,
                        imageRes = R.drawable.img_shoe_blue, // заглушка
                        isFavorite = favSet.contains(p.id),
                        description = p.description
                    )
                }
            } catch (e: IOException) {
                errorMessage = "Ошибка сети: ${e.localizedMessage}"
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    fun toggleFavourite(product: CatalogProduct, newFavState: Boolean) {
        if (sessionUserId == null || token == null) {
            errorMessage = "Необходима авторизация"
            return
        }
        // Оптимистичное обновление UI
        allProducts = allProducts.map {
            if (it.id == product.id) it.copy(isFavorite = newFavState) else it
        }
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService
                if (newFavState) {
                    service.addFavourite(
                        authHeader = "Bearer $token",
                        body = FavouriteRequest(
                            user_id = sessionUserId,
                            product_id = product.id
                        )
                    )
                } else {
                    service.deleteFavourite(
                        authHeader = "Bearer $token",
                        userIdFilter = "eq.$sessionUserId",
                        productIdFilter = "eq.${product.id}"
                    )
                }
            } catch (e: Exception) {
                // Откат при ошибке
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = !newFavState) else it
                }
                errorMessage = "Не удалось обновить избранное"
            }
        }
    }

    val currentCategory = categories.find { it.title == selectedCategory }
    val filteredProducts = allProducts.filter { product ->
        when (currentCategory?.id) {
            null, "all" -> true
            else -> product.categoryId == currentCategory.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = selectedCategory,
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F7FB)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Категории",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category.title
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF48B2E7) else Color.White
                            )
                            .clickable { selectedCategory = category.title }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color(0xFF333333),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading && allProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF48B2E7))
            }
        } else if (errorMessage != null && allProducts.isEmpty()) {
            // Показываем ошибку и кнопку повтора, если нет данных
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = errorMessage ?: "Неизвестная ошибка", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { loadData() }) {
                    Text("Повторить")
                }
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
                items(filteredProducts, key = { it.id }) { product ->
                    Box(
                        modifier = Modifier.clickable {
                            navController.navigate("details/${product.id}")
                        }
                    ) {
                        CatalogProductCard(
                            product = product,
                            onToggleFavorite = { p, newState ->
                                toggleFavourite(p, newState)
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог ошибок (пункт 12)
    if (errorMessage != null && !isLoading && allProducts.isNotEmpty()) {
        // Если ошибка появилась после загрузки данных, показываем диалог
        Dialog(onDismissRequest = { errorMessage = null }) {
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
                        text = errorMessage ?: "",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { errorMessage = null },
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
private fun CatalogProductCard(
    product: CatalogProduct,
    onToggleFavorite: (CatalogProduct, Boolean) -> Unit
) {
    // Используем remember с ключом, чтобы обновляться при изменении isFavorite извне
    val isFavorite by rememberUpdatedState(newValue = product.isFavorite)

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
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_favorite_border
                    ),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            onToggleFavorite(product, !isFavorite)
                        }
                )
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