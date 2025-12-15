package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.king.builtordermanagement.data.models.CartItem
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    cartTotal: Double,
    onBackClick: () -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCheckout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartBottomBar(
                    total = cartTotal,
                    itemCount = cartItems.sumOf { it.quantity },
                    onCheckout = onCheckout
                )
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = "Your cart is empty",
                subtitle = "Add some products to get started",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = cartItems,
                    key = { _, item -> item.product.id }
                ) { index, item ->
                    CartItemCard(
                        item = item,
                        onUpdateQuantity = { quantity -> onUpdateQuantity(item.product.id, quantity) },
                        onRemove = { onRemoveItem(item.product.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = !dismissState.value,
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = item.product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$${item.product.discountPrice ?: item.product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QuantitySelector(
                            quantity = item.quantity,
                            onQuantityChange = onUpdateQuantity
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = {
                                dismissState.value = true
                                onRemove()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Remove",
                                tint = ErrorColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                modifier = Modifier.size(16.dp)
            )
        }
        
        AnimatedContent(
            targetState = quantity,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { -it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                } else {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "quantity"
        ) { targetQuantity ->
            Text(
                text = targetQuantity.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CartBottomBar(
    total: Double,
    itemCount: Int,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total ($itemCount items)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            GradientButton(
                text = "Proceed to Checkout",
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
