package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king.builtordermanagement.data.models.Order
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    orders: List<Order>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onOrderClick: (Order) -> Unit,
    onRefresh: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else if (orders.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.ShoppingBag,
                title = "No orders yet",
                subtitle = "Your orders will appear here",
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
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.createdAt?.take(10) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OrderStatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${order.itemsCount} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.paymentMethod ?: "COD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }
        }
    }
}

@Composable
private fun OrderStatusChip(status: String) {
    val (backgroundColor, textColor, icon) = when (status.lowercase()) {
        "pending" -> Triple(WarningColor.copy(alpha = 0.1f), WarningColor, Icons.Outlined.Schedule)
        "confirmed" -> Triple(PrimaryColor.copy(alpha = 0.1f), PrimaryColor, Icons.Outlined.CheckCircle)
        "shipped" -> Triple(AccentColor.copy(alpha = 0.1f), AccentColor, Icons.Outlined.LocalShipping)
        "delivered" -> Triple(SuccessColor.copy(alpha = 0.1f), SuccessColor, Icons.Outlined.Done)
        "cancelled" -> Triple(ErrorColor.copy(alpha = 0.1f), ErrorColor, Icons.Outlined.Cancel)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Outlined.Info)
    }
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
