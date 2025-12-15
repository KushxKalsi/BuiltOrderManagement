package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.king.builtordermanagement.data.models.Category
import com.king.builtordermanagement.data.models.Product
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*
import com.king.builtordermanagement.viewmodel.StoreUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: StoreUiState,
    cartItemCount: Int,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRefresh: () -> Unit,
    onSeeAllClick: (String) -> Unit,
    onSeeAllCategories: () -> Unit = {}
) {
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Welcome back! 👋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Discover Products",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(containerColor = SecondaryColor) {
                                    AnimatedCounter(count = cartItemCount)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Banner Section
            item {
                PromoBanner(onShopNowClick = { onSeeAllClick("featured") })
            }
            
            // Categories Section
            item {
                SectionHeader(title = "Categories", onSeeAllClick = onSeeAllCategories)
            }
            
            item {
                if (uiState.isLoadingCategories) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    CategoriesRow(
                        categories = uiState.categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategoryClick = { category ->
                            selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            onCategoryClick(category)
                        }
                    )
                }
            }
            
            // Featured Products Section
            item {
                SectionHeader(title = "Featured Products ✨", onSeeAllClick = { onSeeAllClick("featured") })
            }
            
            item {
                if (uiState.isLoadingFeatured) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    FeaturedProductsRow(
                        products = uiState.featuredProducts,
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart
                    )
                }
            }
            
            // All Products Section
            item {
                SectionHeader(title = "All Products", onSeeAllClick = { onSeeAllClick("all") })
            }
            
            item {
                if (uiState.isLoadingProducts) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProductsGrid(
                        products = if (selectedCategoryId != null) uiState.categoryProducts else uiState.products,
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoBanner(onShopNowClick: () -> Unit = {}) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(160.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryColor,
                            PrimaryVariant,
                            AccentColor.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Special Offer! 🎉",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Up to 50% OFF",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            scale.animateTo(0.95f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                        }
                        onShopNowClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Shop Now", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSeeAllClick) {
            Text("See All", color = PrimaryColor)
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategoryClick: (Category) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                name = category.name,
                imageUrl = category.imageUrl,
                isSelected = selectedCategoryId == category.id,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun FeaturedProductsRow(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) },
                onAddToCart = { onAddToCart(product) },
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
private fun ProductsGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) },
                        onAddToCart = { onAddToCart(product) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
