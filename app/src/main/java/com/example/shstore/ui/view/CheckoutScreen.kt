package com.example.shstore.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavHostController) {

    val context = LocalContext.current
    val userId = UserSession.userId
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var isEditingEmail by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }

    var cartItems by remember { mutableStateOf<List<CartUiItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) tryGetGpsAddress(context) { if (it.isNotBlank()) address = it }
    }

    LaunchedEffect(Unit) {

        if (userId == null) return@LaunchedEffect

        isLoading = true

        try {

            val service = RetrofitInstance.userManagementService

            val profiles = service.getProfile("eq.$userId")
            profiles.firstOrNull()?.let {
                email = UserSession.email ?: ""
                phone = it.phone ?: ""
                address = it.address ?: ""
            }

            val raw = service.getCartItems("eq.$userId")

            cartItems = raw.map {
                CartUiItem(
                    cartId = it.id,
                    productId = it.product_id,
                    title = it.products?.title ?: "Товар",
                    price = it.products?.cost ?: 0.0,
                    count = it.count ?: 1L
                )
            }

        } catch (e: IOException) {
            errorMessage = "Нет соединения с интернетом"
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки"
        } finally {
            isLoading = false
        }

        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            tryGetGpsAddress(context) { if (it.isNotBlank()) address = it }
        } else {
            locationPermLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    fun placeOrder() {

        if (userId == null) return

        isSaving = true

        scope.launch {

            try {

                val service = RetrofitInstance.userManagementService

                val orderResponse = service.createOrder(
                    CreateOrderRequest(
                        email = email,
                        phone = phone,
                        address = address,
                        user_id = userId,
                        delivery_coast = 60
                    )
                )

                val orderId =
                    orderResponse.body()?.firstOrNull()?.id
                        ?: run {
                            errorMessage = "Ошибка создания заказа"
                            return@launch
                        }

                service.createOrderItems(
                    cartItems.map {
                        CreateOrderItemRequest(
                            title = it.title,
                            coast = it.price,
                            count = it.count,
                            order_id = orderId,
                            product_id = it.productId
                        )
                    }
                )

                service.clearCart("eq.$userId")

                showSuccessDialog = true

            } catch (e: IOException) {
                errorMessage = "Нет соединения с интернетом"
            } catch (e: Exception) {
                errorMessage = "Ошибка"
            } finally {
                isSaving = false
            }
        }
    }

    val subtotal = cartItems.sumOf { it.price * it.count }
    val delivery = 60.20
    val total = subtotal + delivery

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if (isLoading) {

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                ContactInfoRow(
                    iconRes = R.drawable.ic_email,
                    value = email,
                    hint = "Email",
                    isEditing = isEditingEmail,
                    onValueChange = { email = it },
                    onEditClick = { isEditingEmail = !isEditingEmail },
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(10.dp))

                ContactInfoRow(
                    iconRes = R.drawable.ic_phone,
                    value = phone,
                    hint = "Телефон",
                    isEditing = isEditingPhone,
                    onValueChange = { phone = it },
                    onEditClick = { isEditingPhone = !isEditingPhone },
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(10.dp))

                ContactInfoRow(
                    iconRes = R.drawable.ic_location,
                    value = address,
                    hint = "Адрес",
                    isEditing = isEditingAddress,
                    onValueChange = { address = it },
                    onEditClick = { isEditingAddress = !isEditingAddress }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Итого: ₽${"%.2f".format(total)}")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { placeOrder() },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Подтвердить")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showSuccessDialog) {

        Dialog(onDismissRequest = {}) {

            Card(shape = RoundedCornerShape(20.dp)) {

                Column(
                    modifier = Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text("Заказ оформлен")

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Вернуться")
                    }
                }
            }
        }
    }

    if (errorMessage != null) {
        ErrorDialog(errorMessage!!) { errorMessage = null }
    }
}

@Composable
private fun ContactInfoRow(
    iconRes: Int,
    value: String,
    hint: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {

    if (isEditing) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                IconButton(onClick = onEditClick) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                }
            }
        )

    } else {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEditClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(iconRes),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {

                Text(value.ifBlank { "—" })

                Text(
                    hint,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun tryGetGpsAddress(
    context: Context,
    onResult: (String) -> Unit
) {

    val hasFine =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val hasCoarse =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!hasFine && !hasCoarse) return

    try {

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider =
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                LocationManager.GPS_PROVIDER
            else
                LocationManager.NETWORK_PROVIDER

        val location = lm.getLastKnownLocation(provider) ?: return

        val geocoder = Geocoder(context, Locale("ru"))

        val addresses =
            geocoder.getFromLocation(location.latitude, location.longitude, 1)

        val addr = addresses?.firstOrNull() ?: return

        val parts = listOfNotNull(
            addr.locality,
            addr.thoroughfare,
            addr.subThoroughfare
        )

        val result = parts.joinToString(", ")

        if (result.isNotBlank()) onResult(result)

    } catch (_: Exception) {
    }
}