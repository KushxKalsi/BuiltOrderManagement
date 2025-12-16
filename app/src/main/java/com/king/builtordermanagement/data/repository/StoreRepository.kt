package com.king.builtordermanagement.data.repository

import com.king.builtordermanagement.data.api.RetrofitClient
import com.king.builtordermanagement.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository {
    private val api = RetrofitClient.apiService
    
    // Auth
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.success && response.user != null) {
                Result.success(response.user)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(name: String, email: String, password: String, phone: String?, address: String?): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(RegisterRequest(name, email, password, phone, address))
            if (response.success && response.user != null) {
                Result.success(response.user)
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Products
    suspend fun getProducts(page: Int = 1, limit: Int = 20): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts(page, limit)
            if (response.success) {
                Result.success(response.products ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFeaturedProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFeaturedProducts()
            if (response.success) {
                Result.success(response.products ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch featured products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(categoryId: Int): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductsByCategory(categoryId)
            if (response.success) {
                Result.success(response.products ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch products"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchProducts(query)
            if (response.success) {
                Result.success(response.products ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Categories
    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.success) {
                Result.success(response.categories ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Orders
    suspend fun createOrder(
        userId: Int,
        totalAmount: Double,
        shippingAddress: String,
        paymentMethod: String,
        notes: String?,
        items: List<CartItem>,
        discountAmount: Double = 0.0,
        couponCode: String? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val orderItems = items.map { 
                OrderItemRequest(
                    productId = it.product.id,
                    quantity = it.quantity,
                    price = it.product.discountPrice ?: it.product.price
                )
            }
            val request = OrderRequest(
                userId = userId, 
                totalAmount = totalAmount, 
                discountAmount = discountAmount,
                couponCode = couponCode,
                shippingAddress = shippingAddress, 
                paymentMethod = paymentMethod, 
                notes = notes, 
                items = orderItems
            )
            val response = api.createOrder(request)
            if (response.success && response.orderId != null) {
                Result.success(response.orderId)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserOrders(userId: Int): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserOrders(userId)
            if (response.success) {
                Result.success(response.orders ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrderDetail(orderId: Int): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrderDetail(orderId)
            if (response.success && response.order != null) {
                Result.success(response.order)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch order details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelOrder(orderId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.cancelOrder(mapOf("order_id" to orderId))
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Failed to cancel order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Coupons
    suspend fun validateCoupon(code: String, orderAmount: Double, userId: Int?): Result<Coupon> = withContext(Dispatchers.IO) {
        try {
            val response = api.validateCoupon(CouponValidateRequest(code, orderAmount, userId))
            if (response.success && response.coupon != null) {
                Result.success(response.coupon)
            } else {
                Result.failure(Exception(response.message ?: "Invalid coupon"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveCoupons(): Result<List<Coupon>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveCoupons()
            if (response.success) {
                Result.success(response.coupons ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch coupons"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
