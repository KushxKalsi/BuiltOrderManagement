package com.king.builtordermanagement.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.king.builtordermanagement.data.local.UserPreferences
import com.king.builtordermanagement.data.models.*
import com.king.builtordermanagement.data.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = StoreRepository()
    private val userPreferences = UserPreferences(application)
    
    // UI State
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()
    
    // User state
    val currentUser = userPreferences.currentUser.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val isLoggedIn = userPreferences.isLoggedIn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    
    // Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    // Wishlist
    private val _wishlistItems = MutableStateFlow<List<Product>>(emptyList())
    val wishlistItems: StateFlow<List<Product>> = _wishlistItems.asStateFlow()
    
    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { (it.product.discountPrice ?: it.product.price) * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        loadCategories()
        loadFeaturedProducts()
        loadProducts()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            repository.getCategories().fold(
                onSuccess = { categories ->
                    _uiState.update { it.copy(categories = categories, isLoadingCategories = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingCategories = false) }
                }
            )
        }
    }
    
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFeatured = true) }
            repository.getFeaturedProducts().fold(
                onSuccess = { products ->
                    _uiState.update { it.copy(featuredProducts = products, isLoadingFeatured = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingFeatured = false) }
                }
            )
        }
    }
    
    fun loadProducts(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true) }
            repository.getProducts(page).fold(
                onSuccess = { products ->
                    _uiState.update { it.copy(products = products, isLoadingProducts = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingProducts = false) }
                }
            )
        }
    }
    
    fun loadProductsByCategory(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true) }
            repository.getProductsByCategory(categoryId).fold(
                onSuccess = { products ->
                    _uiState.update { it.copy(categoryProducts = products, isLoadingProducts = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingProducts = false) }
                }
            )
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            repository.searchProducts(query).fold(
                onSuccess = { products ->
                    _uiState.update { it.copy(searchResults = products, isSearching = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isSearching = false) }
                }
            )
        }
    }
    
    // Auth functions
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.login(email, password).fold(
                onSuccess = { user ->
                    userPreferences.saveUser(user)
                    _uiState.update { it.copy(isLoading = false) }
                    onResult(true, null)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    onResult(false, error.message)
                }
            )
        }
    }
    
    fun register(name: String, email: String, password: String, phone: String?, address: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.register(name, email, password, phone, address).fold(
                onSuccess = { user ->
                    userPreferences.saveUser(user)
                    _uiState.update { it.copy(isLoading = false) }
                    onResult(true, null)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    onResult(false, error.message)
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUser()
            _cartItems.value = emptyList()
        }
    }
    
    // Cart functions
    fun addToCart(product: Product) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }
        
        if (existingItem != null) {
            existingItem.quantity++
            _cartItems.value = currentCart.toList()
        } else {
            _cartItems.value = currentCart + CartItem(product, 1)
        }
    }
    
    fun removeFromCart(productId: Int) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }
    
    fun updateCartItemQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        
        _cartItems.value = _cartItems.value.map { item ->
            if (item.product.id == productId) {
                item.copy(quantity = quantity)
            } else item
        }
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }
    
    // Wishlist functions
    fun addToWishlist(product: Product) {
        if (!_wishlistItems.value.any { it.id == product.id }) {
            _wishlistItems.value = _wishlistItems.value + product
        }
    }
    
    fun removeFromWishlist(productId: Int) {
        _wishlistItems.value = _wishlistItems.value.filter { it.id != productId }
    }
    
    fun toggleWishlist(product: Product): Boolean {
        val isInWishlist = _wishlistItems.value.any { it.id == product.id }
        if (isInWishlist) {
            removeFromWishlist(product.id)
        } else {
            addToWishlist(product)
        }
        return !isInWishlist
    }
    
    fun isInWishlist(productId: Int): Boolean {
        return _wishlistItems.value.any { it.id == productId }
    }
    
    // Order functions
    fun placeOrder(shippingAddress: String, paymentMethod: String, notes: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                onResult(false, "Please login to place order")
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            repository.createOrder(
                userId = user.id,
                totalAmount = cartTotal.value,
                shippingAddress = shippingAddress,
                paymentMethod = paymentMethod,
                notes = notes,
                items = _cartItems.value
            ).fold(
                onSuccess = { orderId ->
                    clearCart()
                    _uiState.update { it.copy(isLoading = false) }
                    onResult(true, "Order #$orderId placed successfully!")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    onResult(false, error.message)
                }
            )
        }
    }
    
    fun loadUserOrders() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            _uiState.update { it.copy(isLoadingOrders = true) }
            repository.getUserOrders(user.id).fold(
                onSuccess = { orders ->
                    _uiState.update { it.copy(orders = orders, isLoadingOrders = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoadingOrders = false) }
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class StoreUiState(
    val isLoading: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val isLoadingFeatured: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val isLoadingOrders: Boolean = false,
    val isSearching: Boolean = false,
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val products: List<Product> = emptyList(),
    val categoryProducts: List<Product> = emptyList(),
    val searchResults: List<Product> = emptyList(),
    val orders: List<Order> = emptyList(),
    val error: String? = null
)
