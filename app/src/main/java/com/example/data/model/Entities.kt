package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val author: String,
    val category: String,
    val subcategory: String,
    val price: Double,
    val discountPrice: Double,
    val rating: Float,
    val reviewsCount: Int,
    val imageUrl: String, // Can represent local image identifier or url
    val stockStatus: String, // "In Stock", "Low Stock", "Out of Stock"
    val stockCount: Int,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val publisher: String = "",
    val pages: Int = 0,
    val brand: String = "", // For stationery
    val specsJson: String = "" // Custom specifications (e.g. "Weight: 80gsm, Cover: Hardback")
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val quantity: Int,
    val isSavedForLater: Boolean = false
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String,
    val timestamp: Long,
    val totalAmount: Double,
    val status: String, // "Order Placed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled"
    val paymentMethod: String,
    val shippingAddress: String,
    val itemsJson: String, // Serialized list of product IDs and quantities
    val trackingUpdatesJson: String // History of status changes with timestamps
)

@Entity(tableName = "addresses")
data class UserAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String // "Order", "Promo", "Stock", "System"
)

data class Review(
    val id: String,
    val productId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val avatarColor: Int
)

data class Coupon(
    val code: String,
    val discountAmount: Double,
    val minOrderValue: Double,
    val description: String
)
