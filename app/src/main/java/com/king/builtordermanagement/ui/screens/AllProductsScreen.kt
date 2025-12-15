package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king.builtordermanagement.data.models.Product
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    title: String,
    products: List<Product>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }
            products.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.ShoppingBag,
                    title = "No products found",
                    subtitle = "Check back later for new products",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(products) { index, product ->
                        val animatedAlpha = remember { Animatable(0f) }
                        val animatedScale = remember { Animatable(0.8f) }
                        
                        LaunchedEffect(product) {
                            animatedAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    delayMillis = index * 50,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            animatedScale.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    delayMillis = index * 50,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            onAddToCart = { onAddToCart(product) },
                            modifier = Modifier.graphicsLayer {
                                alpha = animatedAlpha.value
                                scaleX = animatedScale.value
                                scaleY = animatedScale.value
                            }
                        )
                    }
                }
            }
        }
    }
}
