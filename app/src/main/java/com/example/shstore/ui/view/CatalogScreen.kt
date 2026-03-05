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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.FavouriteRequest
import com.example.shstore.data.service.ProductDto
import kotlinx.coroutines.launch

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
    val description: String = ""        // описание из базы
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

    var allProducts by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(initialCategoryTitle) }
    var isLoading by remember { mutableStateOf(false) }

    Log.d("CATALOG", "sessionUserId=$sessionUserId token=${token?.take(10)}")

    LaunchedEffect(sessionUserId, token) {
        if (token == null || sessionUserId == null) {
            Log.e("CATALOG", "No token or userId, skip loading")
            return@LaunchedEffect
        }
        isLoading = true
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
                    imageRes = R.drawable.img_shoe_blue,
                    isFavorite = favSet.contains(p.id),
                    description = p.description           // берём описание из базы
                )
            }
        } catch (e: Exception) {
            Log.e("CATALOG", "load error", e)
        } finally {
            isLoading = false
        }
    }

    fun toggleFavourite(product: CatalogProduct, isFav: Boolean) {
        if (sessionUserId == null || token == null) {
            Log.e("FAV", "No token/userId")
            return
        }
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService

                if (isFav) {
                    val resp = service.addFavourite(
                        authHeader = "Bearer $token",
                        body = FavouriteRequest(
                            user_id = sessionUserId,
                            product_id = product.id
                        )
                    )
                    Log.d("FAV", "addFavourite code=${resp.code()} err=${resp.errorBody()?.string()}")
                    if (!resp.isSuccessful) {
                        allProducts = allProducts.map {
                            if (it.id == product.id) it.copy(isFavorite = false) else it
                        }
                        return@launch
                    }
                } else {
                    val resp = service.deleteFavourite(
                        authHeader = "Bearer $token",
                        userIdFilter = "eq.$sessionUserId",
                        productIdFilter = "eq.${product.id}"
                    )
                    Log.d("FAV", "deleteFavourite code=${resp.code()} err=${resp.errorBody()?.string()}")
                }

                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
            } catch (e: Exception) {
                Log.e("FAV", "toggle error", e)
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = !isFav) else it
                }
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
                items(filteredProducts, key = { it.id }) { product ->
                    Box(
                        modifier = Modifier.clickable {
                            navController.navigate("details/${product.id}")
                        }
                    ) {
                        CatalogProductCard(
                            product = product,
                            onToggleFavorite = ::toggleFavourite
                        )
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
    var isFavorite by remember(product.id) { mutableStateOf(product.isFavorite) }

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
                            val newValue = !isFavorite
                            isFavorite = newValue
                            onToggleFavorite(product, newValue)
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
