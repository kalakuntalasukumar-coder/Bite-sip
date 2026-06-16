package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {
    // --- Reactive Flows ---
    val allMenuItems: Flow<List<MenuItem>> = appDao.getAllMenuItems()
    val cartItems: Flow<List<CartItem>> = appDao.getCartItems()
    val allOrders: Flow<List<Order>> = appDao.getAllOrders()
    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()

    // --- Menu Items Actions ---
    suspend fun addMenuItem(item: MenuItem) = appDao.insertMenuItem(item)
    suspend fun updateMenuItem(item: MenuItem) = appDao.updateMenuItem(item)
    suspend fun deleteMenuItem(item: MenuItem) = appDao.deleteMenuItem(item)
    suspend fun clearAllMenu() = appDao.deleteAllMenuItems()

    // --- Cart Actions ---
    suspend fun addToCart(item: CartItem) = appDao.insertCartItem(item)
    suspend fun updateCartQuantity(item: CartItem) = appDao.insertCartItem(item) // inserts/replaces with updated quantity
    suspend fun removeFromCart(item: CartItem) = appDao.deleteCartItem(item)
    suspend fun removeCartItemById(id: Int) = appDao.deleteCartItemById(id)
    suspend fun clearCart() = appDao.clearCart()

    // --- Orders Actions ---
    suspend fun placeOrder(order: Order): Long = appDao.insertOrder(order)
    fun getOrderById(orderId: Int): Flow<Order?> = appDao.getOrderById(orderId)
    suspend fun updateOrderStatus(orderId: Int, status: String) = appDao.updateOrderStatus(orderId, status)

    // --- User Actions ---
    suspend fun saveUserProfile(profile: UserProfile) = appDao.insertUserProfile(profile)
    suspend fun updateUserProfile(profile: UserProfile) = appDao.updateUserProfile(profile)

    // --- Initial Pre-population checks ---
    suspend fun prepopulateDatabaseIfRequired() {
        val currentMenu = allMenuItems.firstOrNull()
        if (currentMenu.isNullOrEmpty()) {
            val defaultMenu = getDefaultMenu()
            appDao.insertAllMenuItems(defaultMenu)
        }
        
        val currentProfile = userProfile.firstOrNull()
        if (currentProfile == null) {
            appDao.insertUserProfile(UserProfile()) // Insert default guest account
        }
    }

    private fun getDefaultMenu(): List<MenuItem> {
        return listOf(
            // Burgers
            MenuItem(
                name = "Crispy Veg Burger",
                description = "Golden potato patty fried to perfection, layered with fresh crunchy lettuce, sliced tomatoes, creamy eggless mayonnaise, and a hint of house spices in a soft toasted sesame bun.",
                price = 69.0,
                category = "Burgers",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60",
                rating = 4.5,
                salesCount = 142
            ),
            MenuItem(
                name = "Crispy Chicken Burger",
                description = "Crispy juicy chicken breast patty, batter-fried and seasoned with herbs, served with crisp lettuce, fresh onions, pickled slices, and tangy garlic mayo on grilled buns.",
                price = 79.0,
                category = "Burgers",
                imageUrl = "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=500&auto=format&fit=crop&q=60",
                rating = 4.6,
                salesCount = 210
            ),
            MenuItem(
                name = "Spicy Chicken Burger",
                description = "Hot and spicy zesty chicken tender thigh patty drenched in peri-peri sauce, topped with high-melt pepper jack cheese, sizzling jalapeños, and layered with spicy chipotle aioli.",
                price = 79.0,
                category = "Burgers",
                imageUrl = "https://images.unsplash.com/photo-1513185158878-8d8c2a2a3bf3?w=500&auto=format&fit=crop&q=60",
                rating = 4.7,
                salesCount = 189
            ),

            // Fries
            MenuItem(
                name = "French Fries",
                description = "Classic thin-cut potatoes, salted to perfection and fried until super crispy on the outside while remaining light, fluffy, and tender on the inside.",
                price = 79.0,
                category = "Fries",
                imageUrl = "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=500&auto=format&fit=crop&q=60",
                rating = 4.3,
                salesCount = 325,
                isRecommendedAddon = true
            ),
            MenuItem(
                name = "Peri Peri Fries",
                description = "Golden-fried crispy french fries heavily dusted with our iconic hot and tangy, freshly-ground African Bird's Eye dry Peri Peri spice seasoning.",
                price = 89.0,
                category = "Fries",
                imageUrl = "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?w=500&auto=format&fit=crop&q=60",
                rating = 4.4,
                salesCount = 280,
                isRecommendedAddon = true
            ),
            MenuItem(
                name = "Cheese Fries",
                description = "Signature premium potato fries fully smothered with hot, velvety, rich liquid cheddar cheese sauce and garnished with a touch of chopped dried herbs.",
                price = 99.0,
                category = "Fries",
                imageUrl = "https://images.unsplash.com/photo-1585109649139-366815a0d713?w=500&auto=format&fit=crop&q=60",
                rating = 4.6,
                salesCount = 195
            ),
            MenuItem(
                name = "Chicken Nuggets",
                description = "Bite-sized delicious pieces of ground chicken, seasoned with white pepper, breaded lightly, and deep-fried into quick-snack crisp nuggets. Served with sweet chili dip.",
                price = 99.0,
                category = "Fries",
                imageUrl = "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&auto=format&fit=crop&q=60",
                rating = 4.5,
                salesCount = 150
            ),

            // Sandwiches
            MenuItem(
                name = "Vegetable Grill Sandwich",
                description = "Three-tiered wholesome sandwich stuffed with shredded cabbage, sliced cucumbers, boiled potatoes, soft beetroots, and capsicum slices. Grilled with salted butter and green coriander spread.",
                price = 69.0,
                category = "Sandwiches",
                imageUrl = "https://images.unsplash.com/photo-1539252554453-80ab65ce3586?w=500&auto=format&fit=crop&q=60",
                rating = 4.2,
                salesCount = 112
            ),
            MenuItem(
                name = "Paneer Grill Sandwich",
                description = "Crumbles of freshly made Indian cottage cheese (Paneer) tossed in tandoori dry spices, loaded with gooey mozzarella, red capsicums, and charred on whole-wheat bread lines.",
                price = 79.0,
                category = "Sandwiches",
                imageUrl = "https://images.unsplash.com/photo-1521390188846-e2a3a97453a0?w=500&auto=format&fit=crop&q=60",
                rating = 4.5,
                salesCount = 160
            ),
            MenuItem(
                name = "Chicken Grill Sandwich",
                description = "Tender shredded tikka-marinated chicken breast slices, mixed with cooling white mayonnaise, sweet corn kernels, yellow onions, and cheese, pressed in white panini loaves.",
                price = 89.0,
                category = "Sandwiches",
                imageUrl = "https://images.unsplash.com/photo-1475090169767-40ed8d18f67d?w=500&auto=format&fit=crop&q=60",
                rating = 4.6,
                salesCount = 240
            ),

            // Shakes & Mojitos
            MenuItem(
                name = "Oreo Shake",
                description = "Velvety smooth vanilla ice cream, whipped whole milk, and crumbly genuine Oreo cookie chunks blended with chocolate syrup into a thick heavenly milk shake topped with Oreo dust.",
                price = 79.0,
                category = "Shakes & Mojitos",
                imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500&auto=format&fit=crop&q=60",
                rating = 4.8,
                salesCount = 310,
                isRecommendedAddon = true
            ),
            MenuItem(
                name = "Strawberry Shake",
                description = "Cool refreshing blend of thick dairy milk cream, ripe strawberry puree, sweet strawberry flavored syrup, and custom whole fruit chunks.",
                price = 79.0,
                category = "Shakes & Mojitos",
                imageUrl = "https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=500&auto=format&fit=crop&q=60",
                rating = 4.5,
                salesCount = 175,
                isRecommendedAddon = true
            ),
            MenuItem(
                name = "Blue Mojito",
                description = "A gorgeous crystal blue thirst quencher combining soda, sparkling water, sweet blue curacao flavor liqueur, crushed sweet lime wedges, and freshly slapped wild mint leaves.",
                price = 79.0,
                category = "Shakes & Mojitos",
                imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500&auto=format&fit=crop&q=60",
                rating = 4.4,
                salesCount = 205
            ),
            MenuItem(
                name = "Strawberry Mojito",
                description = "Tropical, iced refreshing cooler with sweet strawberries, natural raw sugar, carbonated lime juice, wild mint sprigs, and plenty of crushed ice rocks.",
                price = 79.0,
                category = "Shakes & Mojitos",
                imageUrl = "https://images.unsplash.com/photo-1497534446932-c925b458314e?w=500&auto=format&fit=crop&q=60",
                rating = 4.3,
                salesCount = 144
            ),

            // Combos
            MenuItem(
                name = "Veg Combo Pack",
                description = "The ultimate vegetarian party package! Includes 1x Crispy Veg Burger + 1x Medium French Fries + 1x Choice of Blue or Strawberry chilled Mojito.",
                price = 199.0,
                category = "Combos",
                imageUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?w=500&auto=format&fit=crop&q=60",
                rating = 4.8,
                salesCount = 152
            ),
            MenuItem(
                name = "Non-Veg Combo",
                description = "Satisfy your massive hunger loops! Includes 1x Spicy Chicken Burger + 1x Spicy Peri Peri Fries + 1x Luxurious thick Oreo Chocolate Shake.",
                price = 229.0,
                category = "Combos",
                imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500&auto=format&fit=crop&q=60",
                rating = 4.9,
                salesCount = 245
            )
        )
    }
}
