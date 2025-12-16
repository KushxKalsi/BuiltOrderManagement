package com.king.builtordermanagement.data.api

import com.king.builtordermanagement.data.models.*
import retrofit2.http.*

interface ApiService {
    
    // Auth endpoints
    @POST("auth.php?action=login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>
    
    @POST("auth.php?action=register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>
    
    @POST("auth.php?action=update")
    suspend fun updateProfile(@Body data: Map<String, @JvmSuppressWildcards Any>): ApiResponse<User>
    
    // Products endpoints
    @GET("products.php?action=list")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<Product>
    
    @GET("products.php?action=featured")
    suspend fun getFeaturedProducts(): ApiResponse<Product>
    
    @GET("products.php?action=category")
    suspend fun getProductsByCategory(
        @Query("category_id") categoryId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<Product>
    
    @GET("products.php?action=search")
    suspend fun searchProducts(@Query("q") query: String): ApiResponse<Product>
    
    @GET("products.php?action=detail")
    suspend fun getProductDetail(@Query("id") productId: Int): ApiResponse<Product>
    
    // Categories endpoints
    @GET("categories.php")
    suspend fun getCategories(): ApiResponse<Category>
    
    // Orders endpoints
    @POST("orders.php?action=create")
    suspend fun createOrder(@Body request: OrderRequest): ApiResponse<Order>
    
    @GET("orders.php?action=list")
    suspend fun getUserOrders(@Query("user_id") userId: Int): ApiResponse<Order>
    
    @GET("orders.php?action=detail")
    suspend fun getOrderDetail(@Query("order_id") orderId: Int): ApiResponse<Order>
    
    @POST("orders.php?action=cancel")
    suspend fun cancelOrder(@Body data: Map<String, Int>): ApiResponse<Order>
    
    // Coupon endpoints
    @POST("coupons.php?action=validate")
    suspend fun validateCoupon(@Body request: CouponValidateRequest): CouponResponse
    
    @GET("coupons.php?action=list")
    suspend fun getActiveCoupons(): CouponResponse
}
