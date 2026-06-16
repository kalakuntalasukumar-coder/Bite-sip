package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CafeViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // --- State Stream Observers ---
    val menuItemsList: StateFlow<List<MenuItem>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemsList: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderHistoryList: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Local Screen States ---
    var selectedCategory by mutableStateOf("All")
    var searchQuery by mutableStateOf("")
    var isDarkMode by mutableStateOf(false)
    var activeCouponCode by mutableStateOf("")
    var couponDiscountPercent by mutableStateOf(0.0)
    var couponFlatDiscount by mutableStateOf(0.0)
    var couponMessage by mutableStateOf("")
    var couponError by mutableStateOf("")
    
    // UI Event messages
    var toastMessage by mutableStateOf<String?>(null)

    // Active Tracking Order ID and Step
    var activeTrackingOrderId by mutableStateOf<Int?>(null)
    var activeOrderStatus by mutableStateOf("")
    
    // AI Recommendations State
    var aiRecommendation by mutableStateOf("Try Bite & Sip's Best Seller!\nOur Combos are on hot special today. Super crispy, paired perfectly, saving you up to ₹40!")
    var isAiLoading by mutableStateOf(false)

    // Push Notifications Log (Simulation Drawer)
    private val _pushNotifications = MutableStateFlow<List<PushMessage>>(emptyList())
    val pushNotifications: StateFlow<List<PushMessage>> = _pushNotifications.asStateFlow()

    // Support Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Bite & Sip Bot", "Hello! Welcome to Bite & Sip Support. Let us know if you have any questions about our tasty burgs, status of orders, or special franchise enquiries!", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            // First ensure Room is seeded with the standard menu and a default wallet
            repository.prepopulateDatabaseIfRequired()
            
            // Periodically check if cart changed to fetch custom AI suggestions
            cartItemsList.collect { list ->
                updateAiSuggestions(list)
            }
        }

        // Prepopulate a few mock notifications on start
        _pushNotifications.value = listOf(
            PushMessage("Burger Day Special", "Order any crispy burger today and grab a second for 50% Off! Coupon: BOGO50", System.currentTimeMillis() - 7200000),
            PushMessage("Weekend Treat", "Weekend is here! Get FREE Delivery on orders above ₹149. Code: WEEKEND", System.currentTimeMillis() - 25000000)
        )
    }

    // --- Cart Calculators ---
    val cartSubtotal: StateFlow<Double> = cartItemsList.map { list ->
        list.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartGst: StateFlow<Double> = cartSubtotal.map { subtotal ->
        subtotal * 0.18 // 18% GST (Tax)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDeliveryFee: StateFlow<Double> = cartSubtotal.map { subtotal ->
        if (subtotal == 0.0) 0.0 else if (subtotal >= 199.0) 0.0 else 30.0 // Free delivery above 199
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDiscount: StateFlow<Double> = combine<Double, Double, Double, Double>(
        cartSubtotal,
        snapshotFlow { couponDiscountPercent },
        snapshotFlow { couponFlatDiscount }
    ) { subtotal, pct, flat ->
        val percentOff = subtotal * (pct / 100.0)
        percentOff + flat
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, cartGst, cartDeliveryFee, cartDiscount) { subtotal, gst, delivery, discount ->
        val net = subtotal + gst + delivery - discount
        if (net < 0.0) 0.0 else net
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- UI Actions ---
    
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun searchMenu(query: String) {
        searchQuery = query
    }

    fun selectCategory(category: String) {
        selectedCategory = category
    }

    // --- Cart Core Operations ---
    
    fun addToCart(menuItem: MenuItem, quantity: Int) {
        viewModelScope.launch {
            val existing = cartItemsList.value.find { it.menuItemId == menuItem.id }
            if (existing != null) {
                repository.updateCartQuantity(existing.copy(quantity = existing.quantity + quantity))
                showToast("Updated ${menuItem.name} quantity to ${existing.quantity + quantity} in cart!")
            } else {
                repository.addToCart(
                    CartItem(
                        menuItemId = menuItem.id,
                        name = menuItem.name,
                        price = menuItem.price,
                        imageUrl = menuItem.imageUrl,
                        quantity = quantity,
                        category = menuItem.category
                    )
                )
                showToast("Added ${menuItem.name} (x$quantity) to cart!")
            }
        }
    }

    fun decreaseQuantityInCart(cartItem: CartItem) {
        viewModelScope.launch {
            if (cartItem.quantity <= 1) {
                repository.removeFromCart(cartItem)
                showToast("Removed ${cartItem.name} from cart")
            } else {
                repository.updateCartQuantity(cartItem.copy(quantity = cartItem.quantity - 1))
            }
        }
    }

    fun increaseQuantityInCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItem.copy(quantity = cartItem.quantity + 1))
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(cartItem)
            showToast("Removed ${cartItem.name} from cart")
        }
    }

    // --- Coupon Codes System ---
    fun applyCouponCode(code: String) {
        couponError = ""
        couponMessage = ""
        val subtotal = cartSubtotal.value
        
        if (subtotal == 0.0) {
            couponError = "Add items to cart before applying coupon!"
            return
        }

        val cleanCode = code.trim().uppercase()
        when (cleanCode) {
            "WELCOME50" -> {
                couponFlatDiscount = 50.0
                couponDiscountPercent = 0.0
                activeCouponCode = cleanCode
                couponMessage = "Flat ₹50 OFF applied successfully!"
                showToast("₹50 discount applied!")
            }
            "BITE20" -> {
                if (subtotal >= 149.0) {
                    couponDiscountPercent = 20.0
                    couponFlatDiscount = 0.0
                    activeCouponCode = cleanCode
                    couponMessage = "20% OFF applied successfully!"
                    showToast("20% Cafe coupon applied!")
                } else {
                    couponError = "Minimum order value is ₹149 to use BITE20"
                }
            }
            "LOYAL10" -> {
                couponDiscountPercent = 10.0
                couponFlatDiscount = 0.0
                activeCouponCode = cleanCode
                couponMessage = "10% Loyalist discount applied!"
                showToast("10% Loyalist discount applied!")
            }
            "WEEKEND" -> {
                if (subtotal >= 149.0) {
                    couponFlatDiscount = 30.0 // Discount delivery in effect or flat ₹30 discount
                    couponDiscountPercent = 0.0
                    activeCouponCode = cleanCode
                    couponMessage = "Weekend flat ₹30 discount active!"
                } else {
                    couponError = "Promo code WEEKEND requires ₹149+ minimum"
                }
            }
            else -> {
                couponError = "Invalid coupon code. Try WELCOME50 or BITE20"
            }
        }
    }

    fun removeCoupon() {
        activeCouponCode = ""
        couponDiscountPercent = 0.0
        couponFlatDiscount = 0.0
        couponMessage = ""
        couponError = ""
        showToast("Coupon removed")
    }

    // --- Profile & Wallet Operations ---
    
    fun topUpWallet(amount: Double) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            val updated = profile.copy(walletBalance = profile.walletBalance + amount)
            repository.updateUserProfile(updated)
            showToast("Added ₹$amount to Wallet! Current: ₹${updated.walletBalance}")
        }
    }

    fun saveProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(name = name, phone = phone, savedAddress = address)
            repository.saveUserProfile(updated)
            showToast("Saved profile information successfully!")
        }
    }

    // --- Checkout & Payment Simulation ---
    
    fun checkoutAndPlaceOrder(paymentMethod: String, customAddress: String, onOrderPlaced: (Int) -> Unit) {
        viewModelScope.launch {
            val subtotal = cartSubtotal.value
            val total = cartTotal.value
            val profile = userProfile.value ?: UserProfile()
            
            if (subtotal == 0.0) {
                showToast("Your cart is empty!")
                return@launch
            }

            // Wallet funds validation
            if (paymentMethod == "Wallet" && profile.walletBalance < total) {
                showToast("Insufficient Wallet balance! Top up in your Accounts tab or choose Cash on Delivery.")
                return@launch
            }

            // Build items text summary
            val itemsSummaryText = cartItemsList.value.joinToString { "${it.quantity}x ${it.name}" }

            // Deduct from wallet if wallet selected
            val pointsEarned = (subtotal / 10).toInt() // Earn 1 loyalty point per 10 rupees spent
            val updatedProfile = if (paymentMethod == "Wallet") {
                profile.copy(
                    walletBalance = profile.walletBalance - total,
                    loyaltyPoints = profile.loyaltyPoints + pointsEarned,
                    savedAddress = customAddress.ifBlank { profile.savedAddress }
                )
            } else {
                profile.copy(
                    loyaltyPoints = profile.loyaltyPoints + pointsEarned,
                    savedAddress = customAddress.ifBlank { profile.savedAddress }
                )
            }

            repository.saveUserProfile(updatedProfile)

            // Insert standard Order object
            val newOrder = Order(
                status = "Received",
                subtotal = subtotal,
                gst = cartGst.value,
                deliveryFee = cartDeliveryFee.value,
                discount = cartDiscount.value,
                total = total,
                itemsSummary = itemsSummaryText,
                loyaltyPointsEarned = pointsEarned
            )

            val newId = repository.placeOrder(newOrder).toInt()
            
            // Clear cart & clear active coupon
            repository.clearCart()
            activeCouponCode = ""
            couponDiscountPercent = 0.0
            couponFlatDiscount = 0.0
            
            showToast("Order placed successfully! ₹${"%.1f".format(total)} paid via $paymentMethod.")
            
            // Start Live simulated Order Tracking step increments of 10s back-to-back
            startLiveOrderTracking(newId)
            
            onOrderPlaced(newId)
        }
    }

    // --- Order Status Automatic State Machine ---
    private fun startLiveOrderTracking(orderId: Int) {
        activeTrackingOrderId = orderId
        activeOrderStatus = "Received"
        
        viewModelScope.launch {
            // Received -> Preparing -> Cooking -> Ready -> Out for delivery -> Delivered
            val statuses = listOf("Received", "Preparing", "Cooking", "Ready", "Out for Delivery", "Delivered")
            val statusMessages = listOf(
                "Bite & Sip received your order! Chef is verifying the details.",
                "Your ingredients are fresh and ready. Prep work has started!",
                "Sizzling on the grill! Your burgers and fries are cooking hot.",
                "Your delicious meal is securely packaged in vacuum insulation bags.",
                "Valet rider has picked up your food. He's driving fast to you!",
                "Delivered! Enjoy your warm bite & refreshing sips!"
            )

            for (i in statuses.indices) {
                delay(i * 12000L - (i - 1).coerceAtLeast(0) * 12000L) // updates every 12 seconds
                
                // If the tracking has changed or user dismissed, abort safely
                if (activeTrackingOrderId != orderId) break
                
                val currentStatus = statuses[i]
                activeOrderStatus = currentStatus
                repository.updateOrderStatus(orderId, currentStatus)
                
                // Add push message simulation
                addMockNotification("Order #${orderId} update", statusMessages[i])
            }
        }
    }

    // --- Dynamic AI recommendations ---
    private fun updateAiSuggestions(cartItems: List<CartItem>) {
        viewModelScope.launch {
            isAiLoading = true
            val available = menuItemsList.value
            val cartSummary = if (cartItems.isEmpty()) "empty" else cartItems.joinToString { "${it.quantity}x ${it.name}" }
            
            val pairingResult = GeminiService.getSmartRecommendations(cartSummary, available)
            aiRecommendation = pairingResult
            isAiLoading = false
        }
    }

    // --- In-App Support Chat Simulation ---
    fun sendUserChatMessage(text: String) {
        if (text.isBlank()) return
        
        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(ChatMessage("You", text, true))
        _chatMessages.value = updatedList

        // Coroutine to trigger automated restaurant response
        viewModelScope.launch {
            delay(1200)
            val replyText = getBotReplyForMessage(text)
            val listWithReply = _chatMessages.value.toMutableList()
            listWithReply.add(ChatMessage("Bite & Sip Bot", replyText, false))
            _chatMessages.value = listWithReply
        }
    }

    private fun getBotReplyForMessage(userMsg: String): String {
        val query = userMsg.lowercase()
        return when {
            query.contains("order") || query.contains("track") || query.contains("where") -> {
                "You can see your real-time live map tracking inside 'Track Order' in the main menu hub! If an order shows 'Delivered' but hasn't arrived, please call our hotline +91 98765 43210."
            }
            query.contains("discount") || query.contains("coupon") || query.contains("offer") -> {
                "We have active coupons! Try 'WELCOME50' to get ₹50 flat discount or 'BITE20' for 20% off on premium burgers!"
            }
            query.contains("refund") || query.contains("cancel") || query.contains("money") -> {
                "For immediate cancellations or refunds directly to your Bite & Sip Wallet, please tap 'Call Support' to ping our manager on WhatsApp immediately!"
            }
            query.contains("hello") || query.contains("hi") || query.contains("hey") -> {
                "Hello there! Hungry for food? Try browsing our freshly categories or look at our special Combos!"
            }
            else -> {
                "Thank you for contacting Bite & Sip. Your query has been logged. Our chief café concierge will revert shortly, or you can ping us directly on WhatsApp."
            }
        }
    }

    // --- Admin Dashboard Actions (Room database write) ---
    
    fun adminAddNewMenuItem(name: String, desc: String, category: String, price: Double, image: String) {
        viewModelScope.launch {
            if (name.isBlank() || price <= 0.0) {
                showToast("Invalid menu details entered!")
                return@launch
            }
            val sanitizedImage = image.ifBlank {
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=60" // Default generic food
            }
            val newItem = MenuItem(
                name = name,
                description = desc,
                category = category,
                price = price,
                imageUrl = sanitizedImage,
                salesCount = 0
            )
            repository.addMenuItem(newItem)
            showToast("Added $name successfully to the menu!")
        }
    }

    fun adminEditPrice(item: MenuItem, newPrice: Double) {
        viewModelScope.launch {
            if (newPrice <= 0.0) {
                showToast("Price must be greater than 0!")
                return@launch
            }
            repository.addMenuItem(item.copy(price = newPrice))
            showToast("Updated ${item.name} price to ₹$newPrice")
        }
    }

    fun adminDeleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
            showToast("Removed ${item.name} from the active menu")
        }
    }

    // Mock Notifications
    private fun addMockNotification(title: String, desc: String) {
        val updated = _pushNotifications.value.toMutableList()
        updated.add(0, PushMessage(title, desc, System.currentTimeMillis()))
        _pushNotifications.value = updated
    }

    private fun showToast(msg: String) {
        toastMessage = msg
    }
}

// Model classes
data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class PushMessage(
    val title: String,
    val body: String,
    val timestamp: Long
)
