package com.king.builtordermanagement.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.king.builtordermanagement.data.models.Product
import com.king.builtordermanagement.ui.screens.*
import com.king.builtordermanagement.ui.theme.PrimaryColor
import com.king.builtordermanagement.viewmodel.StoreViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Auth : Screen("auth")
    object ProductDetail : Screen("product/{productId}")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search.route, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Cart.route, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun StoreNavHost(
    navController: NavHostController,
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                cartItemCount = cartItemCount,
                onProductClick = { product ->
                    selectedProduct = product
                    navController.navigate("product/${product.id}")
                },
                onAddToCart = { viewModel.addToCart(it) },
                onCategoryClick = { viewModel.loadProductsByCategory(it.id) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onRefresh = { viewModel.loadInitialData() }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onBackClick = { navController.popBackStack() },
                onSearch = { viewModel.searchProducts(it) },
                onProductClick = { product ->
                    selectedProduct = product
                    navController.navigate("product/${product.id}")
                },
                onAddToCart = { viewModel.addToCart(it) }
            )
        }
        
        composable(Screen.Cart.route) {
            CartScreen(
                cartItems = cartItems,
                cartTotal = cartTotal,
                onBackClick = { navController.popBackStack() },
                onUpdateQuantity = { productId, quantity ->
                    viewModel.updateCartItemQuantity(productId, quantity)
                },
                onRemoveItem = { viewModel.removeFromCart(it) },
                onCheckout = {
                    if (isLoggedIn) {
                        navController.navigate(Screen.Checkout.route)
                    } else {
                        navController.navigate(Screen.Auth.route)
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                user = currentUser,
                isLoggedIn = isLoggedIn,
                onLoginClick = { navController.navigate(Screen.Auth.route) },
                onOrdersClick = { navController.navigate(Screen.Orders.route) },
                onLogout = { viewModel.logout() }
            )
        }
        
        composable(Screen.Auth.route) {
            AuthScreen(
                isLoading = uiState.isLoading,
                onLogin = { email, password, callback ->
                    viewModel.login(email, password) { success, error ->
                        callback(success, error)
                        if (success) navController.popBackStack()
                    }
                },
                onRegister = { name, email, password, phone, address, callback ->
                    viewModel.register(name, email, password, phone, address) { success, error ->
                        callback(success, error)
                        if (success) navController.popBackStack()
                    }
                },
                onSkip = { navController.popBackStack() }
            )
        }
        
        composable("product/{productId}") {
            selectedProduct?.let { product ->
                ProductDetailScreen(
                    product = product,
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { viewModel.addToCart(it) },
                    onBuyNow = { 
                        viewModel.addToCart(it)
                        if (isLoggedIn) {
                            navController.navigate(Screen.Checkout.route)
                        } else {
                            navController.navigate(Screen.Auth.route)
                        }
                    }
                )
            }
        }
        
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                cartItems = cartItems,
                cartTotal = cartTotal,
                currentUser = currentUser,
                isLoading = uiState.isLoading,
                onBackClick = { navController.popBackStack() },
                onPlaceOrder = { address, payment, notes, callback ->
                    viewModel.placeOrder(address, payment, notes) { success, message ->
                        callback(success, message)
                        if (success) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                },
                onLoginRequired = { navController.navigate(Screen.Auth.route) }
            )
        }
        
        composable(Screen.Orders.route) {
            OrdersScreen(
                orders = uiState.orders,
                isLoading = uiState.isLoadingOrders,
                onBackClick = { navController.popBackStack() },
                onOrderClick = { /* Navigate to order detail */ },
                onRefresh = { viewModel.loadUserOrders() }
            )
        }
    }
}

private fun StoreViewModel.loadInitialData() {
    loadCategories()
    loadFeaturedProducts()
    loadProducts()
}
