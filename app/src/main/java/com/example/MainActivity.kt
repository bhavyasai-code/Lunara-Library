package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.LunaraLibraryTheme
import com.example.viewmodel.LibraryViewModel
import com.example.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LibraryViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            LunaraLibraryTheme(mode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

// Global Nav Routes constants
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp"
    const val MAIN = "main"
    const val PRODUCT_DETAILS = "product_details"
    const val CHECKOUT = "checkout"
    const val PAYMENT = "payment"
    const val ORDERS = "orders"
    const val ORDER_TRACKING = "order_tracking"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val THEME_SETTINGS = "theme_settings"
    const val ADMIN_DASHBOARD = "admin_dashboard"
}

@Composable
fun AppNavigation(viewModel: LibraryViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(viewModel, onSplashComplete = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel,
                onOnboardingComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onSkipToMain = {
                    viewModel.signInUser("explorer@lunara.com", "Guest Scholar", "+91 9999999999")
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterNavigate = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.OTP)
                },
                onLoginNavigate = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.OTP) {
            OtpScreen(
                onOtpVerified = {
                    viewModel.signInUser("scholar@example.com", "Scholar", "+91 9900000123")
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        popUpTo(Routes.OTP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainTabsShell(viewModel, parentNavController = navController)
        }

        composable(
            route = "${Routes.PRODUCT_DETAILS}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailsScreen(
                productId = productId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onProductSelected = { newId ->
                    navController.navigate("${Routes.PRODUCT_DETAILS}/$newId")
                }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                viewModel = viewModel,
                onPaymentNavigate = { navController.navigate(Routes.PAYMENT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PAYMENT) {
            PaymentScreen(
                viewModel = viewModel,
                onPaymentSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                    navController.navigate(Routes.ORDERS)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ORDERS) {
            OrderHistoryScreen(
                viewModel = viewModel,
                onTrackOrderNavigate = { navController.navigate(Routes.ORDER_TRACKING) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ORDER_TRACKING) {
            OrderTrackingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.THEME_SETTINGS) {
            ThemeSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Outer visual container housing the Bottom Navigation Tab architecture
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainTabsShell(
    viewModel: LibraryViewModel,
    parentNavController: androidx.navigation.NavHostController
) {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("app_bottom_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    icon = { Icon(if (selectedTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Grand Hall", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_home")
                )

                NavigationBarItem(
                    selected = selectedTab == "category",
                    onClick = { selectedTab = "category" },
                    icon = { Icon(if (selectedTab == "category") Icons.Filled.GridView else Icons.Outlined.GridView, contentDescription = "Categories") },
                    label = { Text("Library Wings", fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_categories")
                )

                NavigationBarItem(
                    selected = selectedTab == "search",
                    onClick = { selectedTab = "search" },
                    icon = { Icon(if (selectedTab == "search") Icons.Filled.Search else Icons.Outlined.Search, contentDescription = "Search") },
                    label = { Text("Discover", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_search")
                )

                NavigationBarItem(
                    selected = selectedTab == "cart",
                    onClick = { selectedTab = "cart" },
                    icon = {
                        val cartItems by viewModel.cart.collectAsState()
                        val activeCount = cartItems.filter { !it.isSavedForLater }.size
                        BadgedBox(
                            badge = {
                                if (activeCount > 0) {
                                    Badge { Text("$activeCount") }
                                }
                            }
                        ) {
                            Icon(if (selectedTab == "cart") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, contentDescription = "Trolley")
                        }
                    },
                    label = { Text("Trolley", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_trolley")
                )

                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" },
                    icon = { Icon(if (selectedTab == "profile") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("My Shelf", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onProductSelected = { id -> parentNavController.navigate("${Routes.PRODUCT_DETAILS}/$id") },
                    onSearchSelected = { selectedTab = "search" },
                    onNotificationsSelected = { parentNavController.navigate(Routes.NOTIFICATIONS) },
                    onAdminSelected = { parentNavController.navigate(Routes.ADMIN_DASHBOARD) }
                )
                "category" -> CategoriesScreen(
                    viewModel = viewModel,
                    onProductSelected = { id -> parentNavController.navigate("${Routes.PRODUCT_DETAILS}/$id") }
                )
                "search" -> SearchScreen(
                    viewModel = viewModel,
                    onProductSelected = { id -> parentNavController.navigate("${Routes.PRODUCT_DETAILS}/$id") }
                )
                "cart" -> CartScreen(
                    viewModel = viewModel,
                    onCheckoutNavigate = { parentNavController.navigate(Routes.CHECKOUT) },
                    onProductSelected = { id -> parentNavController.navigate("${Routes.PRODUCT_DETAILS}/$id") }
                )
                "profile" -> ProfileScreen(
                    viewModel = viewModel,
                    onOrdersNavigate = { parentNavController.navigate(Routes.ORDERS) },
                    onSettingsNavigate = { parentNavController.navigate(Routes.SETTINGS) },
                    onThemeNavigate = { parentNavController.navigate(Routes.THEME_SETTINGS) },
                    onLogoutNavigate = {
                        parentNavController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
