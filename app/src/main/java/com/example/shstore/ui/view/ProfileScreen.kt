package com.example.shstore.ui.view

/**
 * Экран профиля пользователя.
 *
 * Реализовано:
 * - Загрузка данных с сервера при открытии экрана
 * - Кэш в UserSession: поля заполняются мгновенно из кэша,
 *   затем обновляются с сервера — данные не теряются при переходах
 * - Редактирование полей по кнопке карандаша
 * - Сохранение на сервер + обновление кэша в UserSession
 * - Фото: запрос разрешения камеры → открытие камеры → отображение фото
 *   URI фото сохраняется в UserSession и не теряется при выходе из экрана
 *
 * Дата создания: 2025
 * Автор: ShStore Team
 */

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.shstore.R
import com.example.shstore.data.RetrofitInstance
import com.example.shstore.data.UserSession
import com.example.shstore.data.model.UpdateProfileRequest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String,
    accessToken: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }

    // Поля заполняются из кэша UserSession мгновенно — не пустые при переходах
    var firstName by remember { mutableStateOf(UserSession.firstName) }
    var lastName by remember { mutableStateOf(UserSession.lastName) }
    var address by remember { mutableStateOf(UserSession.address) }
    var phone by remember { mutableStateOf(UserSession.phone) }

    // URI фото берётся из UserSession — не теряется при выходе из экрана
    var avatarUri by remember {
        mutableStateOf(
            UserSession.avatarUriString?.let { Uri.parse(it) }
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Камера ────────────────────────────────────────────────────────────────

    // Фиксированный файл для временного хранения фото с камеры
    val tmpImageUri = remember {
        val file = File(context.cacheDir, "profile_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Запускается после съёмки: если фото сделано — сохраняем URI локально и в кэш
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            avatarUri = tmpImageUri
            UserSession.avatarUriString = tmpImageUri.toString()
        }
    }

    // Запрашиваем разрешение на камеру; при выдаче — сразу открываем камеру
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(tmpImageUri)
        } else {
            Toast.makeText(context, "Нужен доступ к камере", Toast.LENGTH_SHORT).show()
        }
    }

    /** Проверить разрешение и открыть камеру */
    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            cameraLauncher.launch(tmpImageUri)
        } else {
            // Показываем системный диалог запроса разрешения
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── Загрузка профиля с сервера ────────────────────────────────────────────
    // Запускается один раз при открытии экрана.
    // Обновляет поля актуальными данными с сервера и кладёт их в кэш.
    LaunchedEffect(userId) {
        isLoading = true
        try {
            val list = RetrofitInstance.userManagementService.getProfile(
                userIdFilter = "eq.$userId"
            )
            list.firstOrNull()?.let { p ->
                firstName = p.firstname.orEmpty()
                lastName = p.lastname.orEmpty()
                address = p.address.orEmpty()
                phone = p.phone.orEmpty()

                // Обновляем кэш актуальными данными с сервера
                UserSession.firstName = firstName
                UserSession.lastName = lastName
                UserSession.address = address
                UserSession.phone = phone
            }
        } catch (e: java.io.IOException) {
            // Нет сети — показываем кэшированные данные без ошибки
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки профиля"
        } finally {
            isLoading = false
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Профиль", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    if (!isEditing) {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF48B2E7))
                                .clickable { isEditing = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Редактировать",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { BottomBar(navController = navController, currentRoute = "profile") },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // ── Аватар ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDDDD))
                        .clickable { if (isEditing) launchCamera() },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        // Отображаем фото сделанное камерой
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = avatarUri,
                                // key нужен чтобы Coil перезагрузил файл после новой съёмки
                                // (файл тот же, но содержимое изменилось)
                            ),
                            contentDescription = "Аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "$firstName $lastName".trim().ifBlank { "Имя пользователя" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                // Кнопка "Изменить фото" видна только в режиме редактирования
                if (isEditing) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Изменить фото профиля",
                        fontSize = 13.sp,
                        color = Color(0xFF48B2E7),
                        modifier = Modifier.clickable { launchCamera() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Штрих-код только в режиме просмотра
                if (!isEditing) {
                    BarcodeCard()
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Поля профиля ──────────────────────────────────────────────
                ProfileField("Имя", firstName, { firstName = it }, isEditing)
                ProfileField("Фамилия", lastName, { lastName = it }, isEditing)
                ProfileField("Адрес", address, { address = it }, isEditing)
                ProfileField("Телефон", phone, { phone = it }, isEditing)

                // ── Кнопка Сохранить ──────────────────────────────────────────
                if (isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                try {
                                    val resp = RetrofitInstance.userManagementService.updateProfile(
                                        userIdFilter = "eq.$userId",
                                        body = UpdateProfileRequest(
                                            firstname = firstName,
                                            lastname = lastName,
                                            address = address,
                                            phone = phone
                                        )
                                    )
                                    if (resp.isSuccessful) {
                                        // Сохраняем актуальные данные в кэш UserSession
                                        // Теперь при повторном входе на экран данные не пропадут
                                        UserSession.firstName = firstName
                                        UserSession.lastName = lastName
                                        UserSession.address = address
                                        UserSession.phone = phone

                                        isEditing = false
                                    } else {
                                        errorMessage = "Ошибка сохранения: ${resp.code()}"
                                    }
                                } catch (e: java.io.IOException) {
                                    errorMessage = "Нет соединения с интернетом"
                                } catch (e: Exception) {
                                    errorMessage = "Ошибка: ${e.localizedMessage}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48B2E7))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Сохранить",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Индикатор загрузки поверх контента
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFF48B2E7)) }
            }
        }
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}

// ── ProfileField ──────────────────────────────────────────────────────────────

@Composable
fun ProfileField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF333333),
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color(0xFF48B2E7),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Text(
                    text = value.ifBlank { "Не указано" },
                    fontSize = 15.sp,
                    color = if (value.isBlank()) Color(0xFFBBBBBB) else Color(0xFF333333)
                )
            }
        }
    }
}

// ── BarcodeCard ───────────────────────────────────────────────────────────────

@Composable
fun BarcodeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F5F5)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Открыть",
                fontSize = 11.sp,
                color = Color(0xFF888888),
                modifier = Modifier.rotate(-90f),
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 10.dp, bottom = 10.dp, end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_barcode),
                contentDescription = "Barcode",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}