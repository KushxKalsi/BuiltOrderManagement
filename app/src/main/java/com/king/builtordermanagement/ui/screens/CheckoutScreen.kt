package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.king.builtordermanagement.data.models.CartItem
import com.king.builtordermanagement.data.models.User
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    cartTotal: Double,
    currentUser: User?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onPlaceOrder: (String, String, String?, (Boolean, String?) -> Unit) -> Unit,
    onLoginRequired: () -> Unit
) {
    var shippingAddress by remember { mutableStateOf(currentUser?.address ?: "") }
    var selectedPaymentMethod by remember { mutableStateOf("COD") }
    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var orderMessage by remember { mutableStateOf("") }
    
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            onLoginRequired()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Order Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.product.name} x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$${String.format("%.2f", (item.product.discountPrice ?: item.product.price) * item.quantity)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$${String.format("%.2f", cartTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }
            }
            
            // Shipping Address
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Shipping Address",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = shippingAddress,
                        onValueChange = { shippingAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your shipping address") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment Method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Payment,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    PaymentOption(
                        title = "Cash on Delivery",
                        subtitle = "Pay when you receive",
                        icon = Icons.Outlined.LocalShipping,
                        isSelected = selectedPaymentMethod == "COD",
                        onClick = { selectedPaymentMethod = "COD" }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PaymentOption(
                        title = "Online Payment",
                        subtitle = "Pay with card or UPI",
                        icon = Icons.Outlined.CreditCard,
                        isSelected = selectedPaymentMethod == "Online",
                        onClick = { selectedPaymentMethod = "Online" }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Note,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Order Notes (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Any special instructions?") },
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Place Order Button
            GradientButton(
                text = "Place Order - $${String.format("%.2f", cartTotal)}",
                onClick = {
                    onPlaceOrder(
                        shippingAddress,
                        selectedPaymentMethod,
                        notes.ifBlank { null }
                    ) { success, message ->
                        if (success) {
                            orderMessage = message ?: "Order placed successfully!"
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                isLoading = isLoading,
                enabled = shippingAddress.isNotBlank()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Order Placed!", fontWeight = FontWeight.Bold) },
            text = { Text(orderMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Continue Shopping")
                }
            }
        )
    }
}

@Composable
private fun PaymentOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
        )
    }
}
