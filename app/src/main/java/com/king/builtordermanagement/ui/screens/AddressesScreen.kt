package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king.builtordermanagement.data.models.Order
import com.king.builtordermanagement.data.models.User
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    user: User?,
    orders: List<Order> = emptyList(),
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Collect unique addresses from user profile and orders
    val allAddresses = remember(user, orders) {
        val addresses = mutableListOf<Pair<String, String>>() // title to address
        
        // Add user's registered address
        if (user?.address != null && user.address.isNotBlank()) {
            addresses.add("Home" to user.address)
        }
        
        // Add unique addresses from orders
        orders.forEach { order ->
            if (order.shippingAddress.isNotBlank() && 
                addresses.none { it.second.equals(order.shippingAddress, ignoreCase = true) }) {
                addresses.add("Order #${order.id}" to order.shippingAddress)
            }
        }
        
        addresses
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        if (allAddresses.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allAddresses) { (title, address) ->
                    AddressCard(
                        title = title,
                        address = address,
                        isDefault = title == "Home",
                        onEdit = { },
                        onDelete = { }
                    )
                }
            }
        } else {
            EmptyState(
                icon = Icons.Outlined.LocationOn,
                title = "No addresses saved",
                subtitle = "Add an address to get started",
                modifier = Modifier.padding(padding)
            )
        }
    }
    
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Address") },
            text = { Text("Address management feature coming soon!") },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun AddressCard(
    title: String,
    address: String,
    isDefault: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isDefault) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Default",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}
