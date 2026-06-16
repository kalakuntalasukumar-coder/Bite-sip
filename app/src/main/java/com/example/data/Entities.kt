package com.example.data

import androidx.room.*

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val rating: Double = 4.5,
    val isRecommendedAddon: Boolean = false,
    val salesCount: Int = 0
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val menuItemId: Int,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int,
    val category: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "Received", "Preparing", "Cooking", "Ready", "Out for Delivery", "Delivered"
    val subtotal: Double,
    val gst: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val itemsSummary: String, // e.g. "2x Crispy Veg Burger, 1x Oreo Shake"
    val loyaltyPointsEarned: Int
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Cafe Guest",
    val phone: String = "+91 98765 43210",
    val savedAddress: String = "123, Luxury High Street, Block B, Bengaluru, Karnataka",
    val walletBalance: Double = 500.0, // Starting default wallet credits
    val loyaltyPoints: Int = 120, // Starting loyalty points
    val isLoggedIn: Boolean = true
)
