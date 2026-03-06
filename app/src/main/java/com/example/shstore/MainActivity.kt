package com.example.shstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shstore.ui.theme.ShStoreTheme
import com.example.shstore.data.UserSession
import com.example.shstore.ui.view.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShStoreTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "onboard1",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboard1") { Onboard1Screen(navController) }
                        composable("onboard2") { Onboard2Screen(navController) }
                        composable("onboard3") { Onboard3Screen(navController) }

                        composable("login") { LoginScreen(navController = navController) }
                        composable("register") { RegisterScreen(navController = navController) }

                        composable("home") { HomeScreen(navController = navController) }

                        // каталог по категории
                        composable(
                            route = "catalog/{category}",
                            arguments = listOf(
                                navArgument("category") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category") ?: "Outdoor"
                            CatalogScreen(
                                navController = navController,
                                initialCategoryTitle = category
                            )
                        }

                        composable("catalog") {
                            CatalogScreen(
                                navController = navController,
                                initialCategoryTitle = "Outdoor"
                            )
                        }

                        composable("favorite") {
                            FavoriteScreen(navController = navController)
                        }

                        composable(
                            route = "details/{productId}",
                            arguments = listOf(
                                navArgument("productId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            DetailsScreen(
                                navController = navController,
                                productId = productId
                            )
                        }

                        composable("profile") {
                            val userId = UserSession.userId
                            val accessToken = UserSession.accessToken
                            if (userId != null && accessToken != null) {
                                ProfileScreen(
                                    navController = navController,
                                    userId = userId,
                                    accessToken = accessToken
                                )
                            } else {
                                LoginScreen(navController = navController)
                            }
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(navController)
                        }

                        composable(
                            route = "verifyOTP/{email}/{type}?name={name}",
                            arguments = listOf(
                                navArgument("email") { type = NavType.StringType },
                                navArgument("type") { type = NavType.StringType },
                                navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val type = backStackEntry.arguments?.getString("type") ?: "signup"
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            VerifyOTPScreen(
                                navController = navController,
                                email = email,
                                otpType = type,
                                name = name
                            )
                        }

                        composable(
                            route = "new_password/{email}",
                            arguments = listOf(
                                navArgument("email") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            NewPasswordScreen(navController = navController, email = email)
                        }

                        // ── Новые маршруты ────────────────────────────────────

                        composable("cart") {
                            CartScreen(navController = navController)
                        }

                        composable("checkout") {
                            CheckoutScreen(navController = navController)
                        }

                        composable("orders") {
                            OrdersScreen(navController = navController)
                        }

                        composable(
                            route = "detail_order/{orderId}",
                            arguments = listOf(
                                navArgument("orderId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                            DetailOrderScreen(
                                navController = navController,
                                orderId = orderId
                            )
                        }
                    }
                }
            }
        }
    }
}