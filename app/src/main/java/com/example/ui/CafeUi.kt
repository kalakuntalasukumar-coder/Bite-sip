package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.CartItem
import com.example.data.MenuItem
import com.example.data.Order
import com.example.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeAppScreen(viewModel: CafeViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("home") }
    var selectedProductForDetail by remember { mutableStateOf<MenuItem?>(null) }
    var showNotificationDrawer by remember { mutableStateOf(false) }
    
    // Auto-dismissing Toast feedback
    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            delay(2500)
            viewModel.toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "B&S",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                        Column {
                            Text(
                                "Bite & Sip",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp
                            )
                            Text(
                                "Café & Grill • Premium Quality",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Notifications Indicator
                    val promoPushes by viewModel.pushNotifications.collectAsStateWithLifecycle()
                    IconButton(
                        onClick = { showNotificationDrawer = !showNotificationDrawer },
                        modifier = Modifier.testTag("notification_toggle")
                    ) {
                        BadgedBox(
                            badge = {
                                if (promoPushes.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                        Text(text = promoPushes.size.toString(), color = Color.Black)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Special Offers Hub",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            val cartItems by viewModel.cartItemsList.collectAsStateWithLifecycle()
            
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.shadow(12.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Default.Restaurant, "Menu", modifier = Modifier.testTag("nav_home")) },
                    label = { Text("Menu", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                
                NavigationBarItem(
                    selected = currentTab == "cart",
                    onClick = { currentTab = "cart" },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(text = cartItems.sumOf { it.quantity }.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, "Basket", modifier = Modifier.testTag("nav_cart"))
                        }
                    },
                    label = { Text("Cart", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                
                NavigationBarItem(
                    selected = currentTab == "orders",
                    onClick = { currentTab = "orders" },
                    icon = { Icon(Icons.Default.History, "Orders", modifier = Modifier.testTag("nav_orders")) },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "account",
                    onClick = { currentTab = "account" },
                    icon = { Icon(Icons.Default.AccountCircle, "Profile", modifier = Modifier.testTag("nav_account")) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "admin",
                    onClick = { currentTab = "admin" },
                    icon = { Icon(Icons.Default.Settings, "Admin", modifier = Modifier.testTag("nav_admin")) },
                    label = { Text("Admin", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen router active tabs
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "home" -> HomeScreenView(
                        viewModel = viewModel,
                        onProductClick = { selectedProductForDetail = it }
                    )
                    "cart" -> CartCheckoutScreenView(
                        viewModel = viewModel,
                        onOrderPlaced = { currentTab = "orders" }
                    )
                    "orders" -> OrdersScreenView(viewModel = viewModel)
                    "account" -> AccountScreenView(viewModel = viewModel)
                    "admin" -> AdminPanelScreenView(viewModel = viewModel)
                }
            }

            // Notification drawer bottom drawer modal
            if (showNotificationDrawer) {
                NotificationOverlay(
                    viewModel = viewModel,
                    onDismiss = { showNotificationDrawer = false }
                )
            }

            // Product Detail Slide-Up panel
            selectedProductForDetail?.let { item ->
                ProductDetailDialog(
                    item = item,
                    viewModel = viewModel,
                    onDismiss = { selectedProductForDetail = null }
                )
            }

            // Global Toast Message Alert
            viewModel.toastMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp)
                        .animateContentSize()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Fastfood, "Alert", tint = Color.White)
                            Text(
                                text = message,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME VIEW
// ==========================================
@Composable
fun HomeScreenView(
    viewModel: CafeViewModel,
    onProductClick: (MenuItem) -> Unit
) {
    val itemsList by viewModel.menuItemsList.collectAsStateWithLifecycle()
    val rawSearch = viewModel.searchQuery
    val activeCat = viewModel.selectedCategory

    // Filter list logic
    val filteredItems = itemsList.filter { item ->
        val matchesSearch = item.name.contains(rawSearch, ignoreCase = true) ||
                item.description.contains(rawSearch, ignoreCase = true) ||
                item.category.contains(rawSearch, ignoreCase = true)
        val matchesCategory = activeCat == "All" || item.category.equals(activeCat, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Large Logo Banner
        item {
            LogoBannerHeader()
        }

        // Promo/Offer slider list
        item {
            PromoSliderBanner()
        }

        // Search Bar container
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(1.dp)
            ) {
                OutlinedTextField(
                    value = rawSearch,
                    onValueChange = { viewModel.searchMenu(it) },
                    placeholder = { Text("What are you craving today?", fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (rawSearch.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchMenu("") }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        // Category Filter pills
        item {
            CategoryHorizontalPills(
                selectedCategory = activeCat,
                onCategorySelected = { viewModel.selectCategory(it) }
            )
        }

        // AI Gourmet pairing suggestion card
        item {
            AiRecommendationBubble(viewModel)
        }

        // Grid Menu Dishes
        if (filteredItems.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.SentimentVeryDissatisfied,
                        "No results",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        "No matching delicacies found!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Try standard categories like Burgers, Fries, or Combos.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Display items grouped by Category or simple vertical cards
            // Category section tags if "All" is selected
            val categoriesGrouped = filteredItems.groupBy { it.category }
            categoriesGrouped.forEach { (catName, catItems) ->
                item {
                    Text(
                        text = catName,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("category_header_$catName")
                    )
                }
                
                items(catItems) { foodItem ->
                    FoodProductCard(
                        item = foodItem,
                        onProductClick = onProductClick,
                        onAddToCartDirect = { viewModel.addToCart(foodItem, 1) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SUBCOMPONENTS: HOME
// ==========================================

@Composable
fun LogoBannerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BiteRed,
                        Color(0xFFB91C1C)
                    )
                )
            )
    ) {
        // Decorative vector graphics simulated
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.5f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.2f)
            )
            drawCircle(
                color = BiteYellow.copy(alpha = 0.15f),
                radius = size.minDimension * 0.3f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    color = BiteYellow,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        "FLAVORS YOU CRAVE",
                        color = BiteBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    "Bite & Sip Gourmet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
                Text(
                    "The premium cafe burger experience",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }

            // Decorative Burger Emoji icon
            Text(
                "🍔🥤",
                fontSize = 44.sp,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
fun PromoSliderBanner() {
    val promos = listOf(
        PromoItem("Burger Bonanza!", "Buy 1 Get 1 on All Crispy Chicken!", "50% Off Today", BiteYellow, Color.Black),
        PromoItem("Weekend Fiesta", "Free delivery above ₹149 + extra ₹30 off", "Code: WEEKEND", BiteRed, Color.White),
        PromoItem("Combos Unleashed", "Save up to 30% on our Veg & Non-veg Packs", "Bite & Save", BiteBlack, Color.White)
    )

    var activeIdx by remember { mutableStateOf(0) }
    
    // Auto-carousels every 6 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            activeIdx = (activeIdx + 1) % promos.size
        }
    }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        val backgroundBrush = when (promos[activeIdx].bgColor) {
            BiteRed -> Brush.linearGradient(listOf(BiteRed, Color(0xFFB91C1C)))
            BiteYellow -> Brush.linearGradient(listOf(BiteYellow, Color(0xFFF1B000)))
            BiteBlack -> Brush.linearGradient(listOf(BiteBlack, Color(0xFF1E1E1E)))
            else -> Brush.linearGradient(listOf(promos[activeIdx].bgColor, promos[activeIdx].bgColor.copy(alpha = 0.8f)))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundBrush)
                .clickable { }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        promos[activeIdx].tag,
                        fontSize = 11.sp,
                        color = promos[activeIdx].textColor.copy(alpha = 0.82f),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        promos[activeIdx].title,
                        fontWeight = FontWeight.Bold,
                        color = promos[activeIdx].textColor,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        promos[activeIdx].subtitle,
                        color = promos[activeIdx].textColor.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = CircleShape,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        "Promotions List",
                        tint = promos[activeIdx].textColor,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }
            }
        }
        
        // Indicator Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            promos.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (idx == activeIdx) 14.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (idx == activeIdx) BiteRed else Color.LightGray)
                )
            }
        }
    }
}

data class PromoItem(
    val title: String,
    val subtitle: String,
    val tag: String,
    val bgColor: Color,
    val textColor: Color
)

@Composable
fun CategoryHorizontalPills(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val list = listOf("All", "Burgers", "Fries", "Sandwiches", "Shakes & Mojitos", "Combos")
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(list) { label ->
            val isActive = label == selectedCategory
            val chipBgColor = if (isActive) BiteYellow else MaterialTheme.colorScheme.surface
            val chipTextColor = if (isActive) BiteBlack else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            
            Surface(
                color = chipBgColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onCategorySelected(label) }
                    .testTag("category_pill_$label"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (label) {
                        "All" -> Icons.Default.AllInclusive
                        "Burgers" -> Icons.Default.Fastfood
                        "Fries" -> Icons.Default.Kitchen
                        "Sandwiches" -> Icons.Default.ListAlt
                        "Shakes & Mojitos" -> Icons.Default.LocalDrink
                        "Combos" -> Icons.Default.LocalOffer
                        else -> Icons.Default.Fastfood
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) BiteBlack else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = chipTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun AiRecommendationBubble(viewModel: CafeViewModel) {
    val reco = viewModel.aiRecommendation
    val isLoading = viewModel.isAiLoading

    Card(
        colors = CardDefaults.cardColors(containerColor = BiteYellow.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BiteYellow.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = BiteYellow,
                shape = CircleShape,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, "AI Assistant", tint = BiteBlack, modifier = Modifier.size(20.dp))
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "VIP Gourmet Recommendation",
                        fontSize = 12.sp,
                        color = BiteRed,
                        fontWeight = FontWeight.Black
                    )
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = BiteRed)
                    } else {
                        Text("Active AI", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reco,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun FoodProductCard(
    item: MenuItem,
    onProductClick: (MenuItem) -> Unit,
    onAddToCartDirect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(1.dp)
            .clickable { onProductClick(item) }
            .testTag("food_card_${item.name.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Image with Async Coil
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, "Rating", tint = BiteYellow, modifier = Modifier.size(14.dp))
                        Text(item.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.description,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${item.price.toInt()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BiteYellow
                    )
                    
                    Button(
                        onClick = { onAddToCartDirect() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_button_${item.id}")
                    ) {
                        Text("ADD +", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: CART / CHECKOUT VIEW
// ==========================================
@Composable
fun CartCheckoutScreenView(
    viewModel: CafeViewModel,
    onOrderPlaced: () -> Unit
) {
    val cartItems by viewModel.cartItemsList.collectAsStateWithLifecycle()
    val subtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val gst by viewModel.cartGst.collectAsStateWithLifecycle()
    val deliveryFee by viewModel.cartDeliveryFee.collectAsStateWithLifecycle()
    val discount by viewModel.cartDiscount.collectAsStateWithLifecycle()
    val total by viewModel.cartTotal.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var activeCouponInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Wallet") } // Wallet or Cash on Delivery
    var customAddressInput by remember { mutableStateOf("") }

    // Init address input once profile is ready
    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (customAddressInput.isBlank()) {
                customAddressInput = it.savedAddress
            }
        }
    }

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                "Empty Basket",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your Bite & Sip Cart is Empty!",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select delectable crispy burgers, sizzling peri peri fries, and premium mocktails from the Menu to start!",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cart_view_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Complete Your Feast",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // List Cart Items
        items(cartItems) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("₹${item.price.toInt()} each", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${(item.price * item.quantity).toInt()}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }

                    // Incrementers
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.decreaseQuantityInCart(item) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, "Decrease quantity", tint = MaterialTheme.colorScheme.primary)
                        }
                        
                        Text(
                            text = item.quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        IconButton(
                            onClick = { viewModel.increaseQuantityInCart(item) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, "Increase quantity", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Delivery Destination Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, "Address", tint = BiteRed)
                        Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    OutlinedTextField(
                        value = customAddressInput,
                        onValueChange = { customAddressInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("cart_address_field"),
                        textStyle = TextStyle(fontSize = 13.sp),
                        placeholder = { Text("Enter deliverable street address details", fontSize = 12.sp) }
                    )
                    Text(
                        "Estimated Delivery Time: 25-35 minutes Max",
                        fontSize = 11.sp,
                        color = Color.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Coupon Code block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Offers & Coupons", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    if (viewModel.activeCouponCode.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = activeCouponInput,
                                onValueChange = { activeCouponInput = it },
                                label = { Text("Coupon Code", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("coupon_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BiteYellow,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Button(
                                onClick = {
                                    viewModel.applyCouponCode(activeCouponInput)
                                    activeCouponInput = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BiteRed)
                            ) {
                                Text("APPLY", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                "Try: WELCOME50 (₹50 off) • BITE20 (20% off)",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Celebration, "Success Promo", tint = Color.Green)
                                Column {
                                    Text("Active: ${viewModel.activeCouponCode}", fontWeight = FontWeight.Black, color = Color.Green, fontSize = 13.sp)
                                    Text(viewModel.couponMessage, fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { viewModel.removeCoupon() }) {
                                Icon(Icons.Default.Cancel, "Remove Coupon", tint = BiteRed)
                            }
                        }
                    }

                    if (viewModel.couponError.isNotEmpty()) {
                        Text(viewModel.couponError, color = BiteRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Payment Method Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = if (selectedPaymentMethod == "Wallet") BiteRed.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selectedPaymentMethod == "Wallet") BiteRed else Color.LightGray),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "Wallet" }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Wallet, "B&S Wallet", tint = BiteRed)
                                Text("B&S Wallet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Bal: ₹${userProfile?.walletBalance?.toInt() ?: 500}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        
                        Surface(
                            color = if (selectedPaymentMethod == "COD") BiteRed.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selectedPaymentMethod == "COD") BiteRed else Color.LightGray),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "COD" }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DirectionsCar, "Cash on Delivery", tint = BiteBlack)
                                Text("Cash/UPI COD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Pay on Delivery", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Receipt Summary Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Payment Summary", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${subtotal.toInt()}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GST (18%)", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${gst.toInt()}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Partner Fee", fontSize = 12.sp, color = Color.Gray)
                        Text(if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}", fontSize = 12.sp, color = if (deliveryFee == 0.0) Color.Green else Color.Black)
                    }
                    if (discount > 0.0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount coupon combo", fontSize = 12.sp, color = Color.Green, fontWeight = FontWeight.Medium)
                            Text("- ₹${discount.toInt()}", fontSize = 12.sp, color = Color.Green, fontWeight = FontWeight.Medium)
                        }
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("₹${total.toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Place Order Button
        item {
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    viewModel.checkoutAndPlaceOrder(selectedPaymentMethod, customAddressInput) {
                        onOrderPlaced()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .testTag("checkout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BiteRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, "Checkout")
                    Text("PLACE ORDER • ₹${total.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: HISTORICAL ORDERS & LIVE TRACKING
// ==========================================
@Composable
fun OrdersScreenView(viewModel: CafeViewModel) {
    val orders by viewModel.orderHistoryList.collectAsStateWithLifecycle()
    val activeTrackId = viewModel.activeTrackingOrderId
    val activeStatus = viewModel.activeOrderStatus

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "My Orders & Tracking",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Live Order Tracking panel if tracking is active
        if (activeTrackId != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp).testTag("live_tracker")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocalShipping, "Shipped", tint = MaterialTheme.colorScheme.primary)
                                Text("Live Order Tracking #${activeTrackId}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { viewModel.activeTrackingOrderId = null }) {
                                Icon(Icons.Default.Close, "Dismiss")
                            }
                        }

                        // Simulated Map visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw simulated roadmap grid
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, size.height*0.3f), androidx.compose.ui.geometry.Offset(size.width, size.height*0.3f), 4f)
                                drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width*0.5f, 0f), androidx.compose.ui.geometry.Offset(size.width*0.5f, size.height), 4f)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Storefront, "Map Cafe Pin", tint = BiteRed, modifier = Modifier.size(24.dp))
                                Text("Bite & Sip delivery valet driving to you...", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Status Progression Timeline
                        val states = listOf("Received", "Preparing", "Cooking", "Ready", "Out for Delivery", "Delivered")
                        val activeIdx = states.indexOf(activeStatus).coerceAtLeast(0)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            states.forEachIndexed { idx, label ->
                                val isDone = idx <= activeIdx
                                val isCurrent = idx == activeIdx
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = if (isCurrent) BiteYellow else if (isDone) Color.Green else Color.LightGray,
                                        shape = CircleShape,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isDone && !isCurrent) {
                                                Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else if (isDone) MaterialTheme.colorScheme.onBackground else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Historic Order Lists
        if (orders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Assignment, "No History", modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No order histories found yet!", fontWeight = FontWeight.Bold)
                    Text("Once you check out items in your cart, logs will appear here.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            items(orders) { o ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Order ID #${o.id}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Surface(
                                color = if (o.status == "Delivered") Color.Green.copy(alpha = 0.15f) else BiteYellow.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = o.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (o.status == "Delivered") Color.DarkGray else BiteRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Text(o.itemsSummary, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Amount Paid", fontSize = 11.sp, color = Color.Gray)
                                Text("₹${o.total.toInt()}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            // Re-order simulation button
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.activeTrackingOrderId = o.id; viewModel.activeOrderStatus = o.status },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("track_btn_${o.id}")
                                ) {
                                    Text("Track Details", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: USER PROFILE / HELP CHAT
// ==========================================
@Composable
fun AccountScreenView(viewModel: CafeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatMessages.collectAsStateWithLifecycle()
    
    var nameEdit by remember { mutableStateOf("") }
    var phoneEdit by remember { mutableStateOf("") }
    var addressEdit by remember { mutableStateOf("") }
    var activeChatInput by remember { mutableStateOf("") }
    
    var topUpFundsText by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            if (nameEdit.isBlank()) nameEdit = it.name
            if (phoneEdit.isBlank()) phoneEdit = it.phone
            if (addressEdit.isBlank()) addressEdit = it.savedAddress
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Loyalty & Concierge Support",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Loyalty Card Visual
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BiteBlack),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .shadow(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Accent background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BiteDarkGray, BiteRed.copy(alpha = 0.3f))
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("BITE & SIP MEMBERSHIP", color = BiteYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(profile?.name ?: "Cafe Guest", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("⭐️ VIP", color = BiteYellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("WALLET BALANCE", color = Color.LightGray, fontSize = 10.sp)
                                Text("₹${profile?.walletBalance?.toInt() ?: 500}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text("LOYALTY POINTS", color = Color.LightGray, fontSize = 10.sp)
                                Text("${profile?.loyaltyPoints ?: 120} PTS", color = BiteYellow, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Fast Wallet Recharge
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Top Up Wallet Balance (Simulated)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = topUpFundsText,
                            onValueChange = { topUpFundsText = it },
                            placeholder = { Text("Amount in ₹", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("topup_input")
                        )
                        Button(
                            onClick = {
                                val amt = topUpFundsText.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    viewModel.topUpWallet(amt)
                                    topUpFundsText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BiteYellow, contentColor = Color.Black)
                        ) {
                            Text("LOAD", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Saved Addresses & details Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account Settings", fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = nameEdit,
                        onValueChange = { nameEdit = it },
                        label = { Text("Full Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneEdit,
                        onValueChange = { phoneEdit = it },
                        label = { Text("Mobile Phone (WhatsApp)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressEdit,
                        onValueChange = { addressEdit = it },
                        label = { Text("Default Delivery Address", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = { viewModel.saveProfile(nameEdit, phoneEdit, addressEdit) },
                        colors = ButtonDefaults.buttonColors(containerColor = BiteRed),
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Helpdesk Chat Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .shadow(1.dp)
                    .testTag("helpdesk_chat")
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BiteRed)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape) {
                                Box(modifier = Modifier.size(10.dp).background(Color.Green, CircleShape).align(Alignment.CenterVertically))
                            }
                            Text("Bite & Sip Chat Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        // Action buttons
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, "Help", tint = Color.White)
                        }
                    }

                    // Chat scroll area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        val state = rememberScrollState()
                        // Auto scrolls down when new messages emit
                        LaunchedEffect(chatHistory.size) {
                            state.animateScrollTo(state.maxValue)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(state),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            chatHistory.forEach { msg ->
                                val side = if (msg.isUser) Alignment.End else Alignment.Start
                                val boxColor = if (msg.isUser) BiteRed else Color.White
                                val txtColor = if (msg.isUser) Color.White else Color.Black
                                
                                Column(modifier = Modifier.align(side)) {
                                    Surface(
                                        color = boxColor,
                                        shape = RoundedCornerShape(12.dp),
                                        shadowElevation = 1.dp
                                    ) {
                                        Text(
                                            text = msg.text,
                                            color = txtColor,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Text(msg.sender, fontSize = 8.sp, color = Color.Gray, modifier = Modifier.align(side).padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = activeChatInput,
                            onValueChange = { activeChatInput = it },
                            placeholder = { Text("Type support query...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("chat_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.sendUserChatMessage(activeChatInput)
                                activeChatInput = ""
                            },
                            modifier = Modifier.testTag("send_chat_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Send message", tint = BiteRed)
                        }
                    }
                }
            }
        }

        // Cafe outlets extra
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Bite & Sip Cafe Outlets v1.2", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("Manager Hotline: +91 98765 43210", fontSize = 12.sp)
                Text("Main Outlet: 100 Ft Rd, Indiranagar, Bengaluru, IN", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ==========================================
// SCREEN 5: OWNER / ADMIN DASHBOARD
// ==========================================
@Composable
fun AdminPanelScreenView(viewModel: CafeViewModel) {
    val menuItems by viewModel.menuItemsList.collectAsStateWithLifecycle()
    
    var categorySelect by remember { mutableStateOf("Burgers") }
    var dishNameInput by remember { mutableStateOf("") }
    var dishPriceInput by remember { mutableStateOf("") }
    var dishDescInput by remember { mutableStateOf("") }
    var dishImageInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Café Owner Dashboard",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Operations metrics summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BiteYellow.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL ITEMS", fontSize = 10.sp, color = Color.Gray)
                        Text(menuItems.size.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ACTIVE ORDERS", fontSize = 10.sp, color = Color.Gray)
                        Text(if (viewModel.activeTrackingOrderId != null) "1 Live" else "0 Idle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SIMULATED SALES", fontSize = 10.sp, color = Color.Gray)
                        Text("₹42,390", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                    }
                }
            }
        }

        // Core Form: Add delicasy
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Delicacy to Menu Catalog", fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = dishNameInput,
                        onValueChange = { dishNameInput = it },
                        label = { Text("Dish Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_dish_name")
                    )
                    OutlinedTextField(
                        value = dishPriceInput,
                        onValueChange = { dishPriceInput = it },
                        label = { Text("Price (INR)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("admin_dish_price")
                    )
                    
                    // Category dropdown or text input
                    Text("Category Selector", fontSize = 11.sp, color = Color.Gray)
                    val cats = listOf("Burgers", "Fries", "Sandwiches", "Shakes & Mojitos", "Combos")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(cats) { c ->
                            val current = c == categorySelect
                            Surface(
                                color = if (current) BiteRed else Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { categorySelect = c }
                            ) {
                                Text(
                                    text = c,
                                    color = if (current) Color.White else Color.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dishDescInput,
                        onValueChange = { dishDescInput = it },
                        label = { Text("Description & Ingredients", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_dish_desc")
                    )
                    OutlinedTextField(
                        value = dishImageInput,
                        onValueChange = { dishImageInput = it },
                        label = { Text("Unsplash Image URL (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("Leaves empty for default yummy burger", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val priceValue = dishPriceInput.toDoubleOrNull() ?: 0.0
                            viewModel.adminAddNewMenuItem(dishNameInput, dishDescInput, categorySelect, priceValue, dishImageInput)
                            // Clear inputs
                            dishNameInput = ""
                            dishPriceInput = ""
                            dishDescInput = ""
                            dishImageInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BiteRed),
                        modifier = Modifier.fillMaxWidth().testTag("admin_add_submit")
                    ) {
                        Text("SAVE DISH TO SQLITE", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Delete / Update active items table list
        item {
            Text("Adjust / Remove Menu Delicacies", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        items(menuItems) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Category: ${item.category}", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${item.price.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Increase price helper button
                        IconButton(
                            onClick = { viewModel.adminEditPrice(item, item.price + 10.0) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, "Increase Price by ₹10", tint = Color.Green)
                        }

                        // Decrease price helper button
                        IconButton(
                            onClick = { viewModel.adminEditPrice(item, (item.price - 10.0).coerceAtLeast(10.0)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.TrendingDown, "Decrease Price by ₹10", tint = BiteRed)
                        }

                        IconButton(
                            onClick = { viewModel.adminDeleteMenuItem(item) },
                            modifier = Modifier.size(32.dp).testTag("delete_dish_${item.id}")
                        ) {
                            Icon(Icons.Default.Delete, "Delete Dish", tint = BiteRed)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBCOMPONENTS: NOTIFICATION HUB OVERLAY
// ==========================================
@Composable
fun NotificationOverlay(
    viewModel: CafeViewModel,
    onDismiss: () -> Unit
) {
    val promoMessages by viewModel.pushNotifications.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(400.dp)
                .clickable(enabled = false) { }
                .testTag("notification_drawer")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Campaign, "Campaign Icon", tint = BiteRed)
                        Text("Active Promos & Deal Alerts", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }
                
                Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (promoMessages.isEmpty()) {
                        item {
                            Text("No alerts active right now! Check back on holidays.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(12.dp))
                        }
                    } else {
                        items(promoMessages) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.LocalOffer, "Promotional code details", tint = BiteRed, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(msg.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(msg.body, fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBCOMPONENTS: PRODUCT SHEETS / DIALOGS
// ==========================================
@Composable
fun ProductDetailDialog(
    item: MenuItem,
    viewModel: CafeViewModel,
    onDismiss: () -> Unit
) {
    var qtySelected by remember { mutableStateOf(1) }
    val addons = listOf("Extra Cheese Slice (+₹15)", "Extra Spicy Patty (+₹35)", "Double Eggless Mayo (+₹10)")
    var addonStates = remember { mutableStateListOf(false, false, false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {}
                .testTag("detail_dialog_${item.id}")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = BiteRed,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = item.category.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(onClick = { onDismiss() }) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                }

                // HD food photo
                item {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, "Ratings", tint = BiteYellow, modifier = Modifier.size(18.dp))
                            Text(item.rating.toString(), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }

                item {
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }

                // Addons checklist
                item {
                    Text("Customize and Add Extra Delight", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                itemsIndexed(addons) { idx, valLabel ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { addonStates[idx] = !addonStates[idx] }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = addonStates[idx],
                            onCheckedChange = { addonStates[idx] = it },
                            colors = CheckboxDefaults.colors(checkedColor = BiteRed)
                        )
                        Text(valLabel, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                // Quantity selector & ADD Button Footer
                item {
                    Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(onClick = { if (qtySelected > 1) qtySelected-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, "Rem Qty")
                            }
                            Text(qtySelected.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            IconButton(onClick = { qtySelected++ }) {
                                Icon(Icons.Default.AddCircleOutline, "Add Qty")
                            }
                        }
                        
                        Button(
                            onClick = {
                                viewModel.addToCart(item, qtySelected)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BiteRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ADD TO CART • ₹${(item.price * qtySelected).toInt()}", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
