package com.king.builtordermanagement.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king.builtordermanagement.data.models.Product
import com.king.builtordermanagement.ui.components.*
import com.king.builtordermanagement.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchResults: List<Product>,
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            if (it.length >= 2) onSearch(it)
                        },
                        placeholder = { Text("Search products...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
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
            isSearching -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }
            searchQuery.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "Search Products",
                    subtitle = "Type to search for products",
                    modifier = Modifier.padding(padding)
                )
            }
            searchResults.isEmpty() && searchQuery.isNotEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "No results found",
                    subtitle = "Try a different search term",
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
                    items(searchResults) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}
