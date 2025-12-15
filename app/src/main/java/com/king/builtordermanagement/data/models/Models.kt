package com.king.builtordermanagement.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val address: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Category(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("image_url") val imageUrl: String? = null,
    val description: String? = null
)

data class Product(
    val id: Int = 0,
    @SerializedName("category_id") val categoryId: Int? = null,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    @SerializedName("discount_price") val discountPrice: Double? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    val stock: Int = 0,
    val rating: Double = 0.0,
    @SerializedName("is_featured") val isFeatured: Boolean = false,
    @SerializedName("category_name") val categoryName: String? = null
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)

data class Order(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    val status: String = "pending",
    @SerializedName("shipping_address") val shippingAddress: String = "",
    @SerializedName("payment_method") val paymentMethod: String? = null,
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("items_count") val itemsCount: Int = 0,
    val items: List<OrderItem>? = null
)

data class OrderItem(
    val id: Int = 0,
    @SerializedName("order_id") val orderId: Int = 0,
    @SerializedName("product_id") val productId: Int = 0,
    val quantity: Int = 0,
    val price: Double = 0.0,
    val name: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

// API Response Models
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val user: T? = null,
    val products: List<T>? = null,
    val categories: List<T>? = null,
    val orders: List<T>? = null,
    val order: T? = null,
    val product: T? = null,
    @SerializedName("order_id") val orderId: Int? = null,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

// Request Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val address: String? = null
)

data class OrderRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("shipping_address") val shippingAddress: String,
    @SerializedName("payment_method") val paymentMethod: String = "COD",
    val notes: String? = null,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("product_id") val productId: Int,
    val quantity: Int,
    val price: Double
)
