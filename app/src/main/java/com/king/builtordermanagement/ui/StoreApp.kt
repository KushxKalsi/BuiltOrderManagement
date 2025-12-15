package com.king.builtordermanagement.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.king.builtordermanagement.ui.navigation.*
import com.king.builtordermanagement.ui.theme.*
import com.king.builtordermanagement.viewmodel.StoreViewModel

@Composable
fun StoreApp(viewModel: StoreViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    
    // Routes where bottom nav should be visible
    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Cart.route,
        Screen.Profile.route
    )
    
    val showBottomNav = currentRoute in bottomNavRoutes
    
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.route == Screen.Cart.route && cartItemCount > 0) {
                                            Badge(containerColor = SecondaryColor) {
                                                Text(
                                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryColor,
                                selectedTextColor = PrimaryColor,
                                indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        StoreNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(
                bottom = if (showBottomNav) padding.calculateBottomPadding() else 0.dp
            )
        )
    }
}
