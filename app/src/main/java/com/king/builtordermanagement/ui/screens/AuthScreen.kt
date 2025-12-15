package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onLogin: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onRegister: (String, String, String, String?, String?, (Boolean, String?) -> Unit) -> Unit,
    onSkip: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryColor.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo/Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryColor, AccentColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Store App",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (isLoginMode) "Welcome back!" else "Create your account",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Auth Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        AuthTab(
                            text = "Login",
                            isSelected = isLoginMode,
                            onClick = { isLoginMode = true },
                            modifier = Modifier.weight(1f)
                        )
                        AuthTab(
                            text = "Register",
                            isSelected = !isLoginMode,
                            onClick = { isLoginMode = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "authForm"
                    ) { loginMode ->
                        Column {
                            if (!loginMode) {
                                StoreTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "Full Name",
                                    leadingIcon = Icons.Outlined.Person
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            StoreTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                leadingIcon = Icons.Outlined.Email
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            StoreTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                leadingIcon = Icons.Outlined.Lock,
                                isPassword = true,
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Outlined.VisibilityOff
                                            else Icons.Outlined.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                }
                            )
                            
                            if (!loginMode) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                StoreTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Phone (Optional)",
                                    leadingIcon = Icons.Outlined.Phone
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                StoreTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = "Address (Optional)",
                                    leadingIcon = Icons.Outlined.LocationOn,
                                    singleLine = false
                                )
                            }
                        }
                    }
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage!!,
                            color = ErrorColor,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GradientButton(
                        text = if (isLoginMode) "Login" else "Create Account",
                        onClick = {
                            errorMessage = null
                            if (isLoginMode) {
                                onLogin(email, password) { success, error ->
                                    if (!success) errorMessage = error
                                }
                            } else {
                                onRegister(
                                    name, email, password,
                                    phone.ifBlank { null },
                                    address.ifBlank { null }
                                ) { success, error ->
                                    if (!success) errorMessage = error
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading,
                        enabled = email.isNotBlank() && password.isNotBlank() &&
                                (isLoginMode || name.isNotBlank())
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onSkip) {
                Text(
                    "Skip for now",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AuthTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryColor else Color.Transparent,
        animationSpec = tween(300),
        label = "tabBg"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
