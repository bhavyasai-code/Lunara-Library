package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.data.model.NotificationItem
import com.example.data.model.Product
import com.example.viewmodel.LibraryViewModel
import com.example.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LibraryViewModel,
    onProductSelected: (Int) -> Unit,
    onSearchSelected: () -> Unit,
    onNotificationsSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val wishlistItems by viewModel.wishlist.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    val unreadCount = notifications.count { !it.isRead }
    val featuredProducts = productsList.filter { it.isFeatured }
    val bestSellers = productsList.filter { it.isBestSeller }
    val newArrivals = productsList.filter { it.isNewArrival }

    val categories = listOf("All", "Books", "Stationery")
    var activeCategory by remember { mutableStateOf("All") }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
    }

    // Dynamic Title & Tagline describing the magical setting shift
    val appHeaderTitle = if (isDark) "THE MOONLIT LIBRARY" else "THE SUNLIT LIBRARY"
    val appHeaderTagline = if (isDark) "🌙 Enchanted in Silver Lanterns" else "☀️ Illuminated in Warm Oak"

    // Shifting thematic banner details matching the exact 100% identical room at different timings
    val bannerImageRes = if (isDark) {
        R.drawable.img_library_night_1781675306292
    } else {
        R.drawable.img_library_day_1781675286820
    }
    val bannerPromoLabel = if (isDark) "Velvet Midnight Reading 🌙" else "Sunlit Golden Archive ☀️"
    val bannerHeadline = if (isDark) "Stories Under Enchanted Lanterns" else "Knowledge Streamed in Sunbeams"
    val bannerCaption = if (isDark) "Enjoy flat ₹100 off on all moon-bound items with LUNARA100" else "Enjoy flat ₹100 off on all scholar scrolls with LUNARA100"

    // Filter our 6 beautifully curated bookstore shelves
    // 1. Grand Hall Shelf (Featured Literary Treasures)
    val grandHallTreasures = featuredProducts.filter { !it.category.equals("Stationery", ignoreCase = true) }
    
    // 2. Scholar's Corner (Academic, UPSC, and study guides)
    val scholarsCorner = productsList.filter { 
        it.category.equals("Books", ignoreCase = true) && 
        (it.subcategory.contains("Academic", ignoreCase = true) || it.subcategory.contains("Competitive", ignoreCase = true))
    }

    // 3. Enchanted Shelf (Finest masterpieces and high ratings >= 4.8)
    val enchantedShelf = productsList.filter { 
        it.subcategory.contains("Novels", ignoreCase = true) || it.rating >= 4.8f
    }

    // 4. New Arrivals Wing (Freshly Inscribed scrolls)
    val newArrivalsWing = newArrivals

    // 5. Stationery Workshop (Scribes and sketchers inventory)
    val stationeryWorkshop = productsList.filter { it.category.equals("Stationery", ignoreCase = true) }

    // 6. Artisan's Desk (Bestselling fine instruments and ledger books)
    val artisansDesk = stationeryWorkshop.filter { it.isBestSeller || it.isFeatured }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant Header Action Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = appHeaderTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = if (isDark) Color(0xFF00E5FF) else DayPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (userSession.isLoggedIn) "Archivist ${userSession.name} • Active" else appHeaderTagline,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    if (userSession.isAdmin) {
                        IconButton(
                            onClick = onAdminSelected,
                            modifier = Modifier.testTag("admin_dashboard_btn")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Area", tint = if (isDark) Color(0xFF00E5FF) else DayPrimary)
                        }
                    }
                    IconButton(
                        onClick = onNotificationsSelected,
                        modifier = Modifier.testTag("notification_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$unreadCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notification Alerts")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Scrollable Hub Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Search Bar Quick Trigger Card
                item {
                    Card(
                        modifier = Modifier
                            .testTag("home_search_bar")
                            .fillMaxWidth()
                            .clickable { onSearchSelected() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Search the ancient halls...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Immersive Hero Banner with shifting day & night background scenery
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Image(
                            painter = painterResource(id = bannerImageRes),
                            contentDescription = "Magical Library Hub",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Rich scrim overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                        startY = 100f
                                    )
                                )
                        )

                        // Slogan overlays
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                bannerPromoLabel,
                                color = if (isDark) Color(0xFF00E5FF) else DayPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                bannerHeadline,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                bannerCaption,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Quick Category Filters Carousel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = activeCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeCategory = cat
                                    viewModel.selectedCategory.value = cat
                                },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("filter_chip_$cat"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // 1. Grand Hall Shelf (Featured Literary Treasures)
                item {
                    BookShelfCarousel(
                        title = "The Grand Hall Shelf",
                        products = grandHallTreasures,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                // 2. Scholar's Corner (Academic and Exam Core references)
                item {
                    BookShelfCarousel(
                        title = "The Scholar\\'s Corner",
                        products = scholarsCorner,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                // 3. The Enchanted Shelf (Top Novel and Collector Editions)
                item {
                    BookShelfCarousel(
                        title = "The Enchanted Shelf",
                        products = enchantedShelf,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                // 4. New Arrivals Wing (Newly inscribed library acquisitions)
                item {
                    BookShelfCarousel(
                        title = "New Arrivals Wing",
                        products = newArrivalsWing,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                // 5. The Stationery Workshop (Inks, pens, ledgers)
                item {
                    BookShelfCarousel(
                        title = "The Stationery Workshop",
                        products = stationeryWorkshop,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                // 6. The Artisan\\'s Desk (Elite handwriting tools and journals)
                item {
                    BookShelfCarousel(
                        title = "The Artisan\\'s Desk",
                        products = artisansDesk,
                        wishlistItems = wishlistItems,
                        onProductSelected = onProductSelected,
                        onAddToCart = { viewModel.addToCart(it) },
                        onWishlistToggle = { viewModel.toggleWishlist(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookShelfCarousel(
    title: String,
    products: List<Product>,
    wishlistItems: List<com.example.data.model.WishlistItem>,
    onProductSelected: (Int) -> Unit,
    onAddToCart: (Int) -> Unit,
    onWishlistToggle: (Int) -> Unit
) {
    if (products.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        SectionHeader(title = title)
        Spacer(modifier = Modifier.height(6.dp))
        
        // Tactile wood texture library shelf backing
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(products) { prod ->
                        val faved = wishlistItems.any { it.productId == prod.id }
                        ProductCard(
                            product = prod,
                            onProductClick = onProductSelected,
                            onAddToCard = { onAddToCart(prod.id) },
                            onWishlistToggle = { onWishlistToggle(prod.id) },
                            isWishlisted = faved,
                            modifier = Modifier.width(165.dp)
                        )
                    }
                }
            }
            
            // Render a real wood shelf plank at the bottom base
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "See All",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: LibraryViewModel,
    onProductSelected: (Int) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val productsList by viewModel.products.collectAsState()

    val subcategories = listOf(
        "Novels", "Academic Books", "Competitive Exam Books", "Children's Books",
        "Magazines", "Pens", "Pencils", "Notebooks", "Journals", "Art Supplies", "Office Supplies"
    )

    var activeSubcat by remember { mutableStateOf("Novels") }
    val filteredProds = productsList.filter { it.subcategory.equals(activeSubcat, ignoreCase = true) }
    val wishlistItems by viewModel.wishlist.collectAsState()

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Magical Vault Categories", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Row(modifier = Modifier.fillMaxSize()) {
                // Left Rails - Subcategory Tabs
                LazyColumn(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(subcategories) { sub ->
                        val isSelected = activeSubcat == sub
                        Box(
                            modifier = Modifier
                                .testTag("cat_tab_$sub")
                                .fillMaxWidth()
                                .clickable { activeSubcat = sub }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = sub,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Grid - Content List
                if (filteredProds.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("This corridor is vacant for now...", textAlign = TextAlign.Center, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentPadding = PaddingValues(bottom = 76.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProds) { prod ->
                            val faved = wishlistItems.any { it.productId == prod.id }
                            ProductCard(
                                product = prod,
                                onProductClick = onProductSelected,
                                onAddToCard = { viewModel.addToCart(prod.id) },
                                onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                                isWishlisted = faved
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: LibraryViewModel,
    onProductSelected: (Int) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val filteredList by viewModel.filteredProducts.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val wishlistItems by viewModel.wishlist.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Search suggestion terms
    val suggestions = listOf("Almanac", "Pens", "UPSC", "Sketch Pencils", "Ayurvedic")

    CelestialBackground(themeMode = themeMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            // Header Search Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("What are you looking for?") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("global_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .testTag("filter_toggle_btn")
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
                }
            }

            // Expanded Collapsible Filters Drawer
            AnimatedVisibility(visible = showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Refining Tools", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Price Thresholds limiters
                        Text("Max Price: ₹${viewModel.maxPrice.collectAsState().value.toInt()}", fontSize = 12.sp)
                        Slider(
                            value = viewModel.maxPrice.collectAsState().value,
                            onValueChange = { viewModel.maxPrice.value = it },
                            valueRange = 100f..2000f,
                            modifier = Modifier.testTag("slider_price")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stock Availability option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Show In Stock Only", fontSize = 12.sp)
                            Switch(
                                checked = viewModel.onlyInStock.collectAsState().value,
                                onCheckedChange = { viewModel.onlyInStock.value = it },
                                modifier = Modifier.testTag("switch_instock")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Suggestions Chips line
            if (query.isEmpty()) {
                Text("Popular Searches", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(suggestions) { sugg ->
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.searchQuery.value = sugg }
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(sugg, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Results List Grid
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No matching books or instruments found.", fontSize = 14.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 76.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { prod ->
                        val faved = wishlistItems.any { it.productId == prod.id }
                        ProductCard(
                            product = prod,
                            onProductClick = onProductSelected,
                            onAddToCard = { viewModel.addToCart(prod.id) },
                            onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                            isWishlisted = faved
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Archival Alerts Inbox") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (notificationsList.isNotEmpty()) {
                        TextButton(onClick = { viewModel.markNotificationsRead() }) {
                            Text("Mark all read", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (notificationsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No scrolls or messages today.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notificationsList) { notif ->
                        Card(
                            modifier = Modifier
                                .testTag("notif_card_${notif.id}")
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead)
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteNotification(notif.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
