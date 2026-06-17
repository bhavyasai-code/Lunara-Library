package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.ui.theme.*
import com.example.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: Int,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onProductSelected: (Int) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val wishlistItems by viewModel.wishlist.collectAsState()

    val product = productsList.find { it.id == productId }
    val isFaved = wishlistItems.any { it.productId == productId }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Scroll missing or non-existent.")
        }
        return
    }

    // Static reviews generator for this product
    val mockReviewsList = remember(productId) {
        listOf(
            Review("r1", productId, "Aarav Sharma", 5, "An absolute masterpiece. The bindings are elegant.", "12 June 2026", 0xFF1E3A8A.toInt()),
            Review("r2", productId, "Priyanka Patel", 4, "High material paper quality, and the packaging was protected with sealing stamps.", "04 June 2026", 0xFF0D9488.toInt()),
            Review("r3", productId, "Sai Kumar", 5, "Essential study kit, highly recommend to CSE scholars.", "28 May 2026", 0xFFF59E0B.toInt())
        )
    }

    val relatedProducts = productsList.filter { it.category == product.category && it.id != product.id }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(product.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                        Icon(
                            imageVector = if (isFaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Faved",
                            tint = if (isFaved) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Dynamic detail scrolls
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Large Cover Gallery
                item {
                    val context = LocalContext.current
                    val imgRes = when (product.imageUrl) {
                        "book_celestial" -> R.drawable.img_app_logo
                        "stat_journal" -> R.drawable.img_library_banner
                        "stat_pen_amber" -> R.drawable.img_app_logo
                        "book_upsc" -> R.drawable.img_library_banner
                        else -> R.drawable.img_app_logo
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imgRes)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = product.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Core details table
                item {
                    Column {
                        Text(
                            text = if (product.brand.isNotEmpty()) product.brand else product.author,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rating row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            M3RatingBar(rating = product.rating, starSize = 18.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.rating}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${product.reviewsCount} verified reviews",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Core Pricing display
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Premium Price", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val originalPrice = product.price
                                    val discountedPrice = product.discountPrice
                                    if (discountedPrice > 0f) {
                                        Text(
                                            text = "₹${discountedPrice.toInt()}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "₹${originalPrice.toInt()}",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val savePr = (originalPrice - discountedPrice).toInt()
                                        Text(
                                            text = "Save ₹$savePr",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    } else {
                                        Text(
                                            text = "₹${originalPrice.toInt()}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Dynamic Stock Badge
                            val statusBgColor = when (product.stockStatus) {
                                "In Stock" -> Color(0xFFE8F5E9)
                                "Low Stock" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            }
                            val statusTextColor = when (product.stockStatus) {
                                "In Stock" -> Color(0xFF2E7D32)
                                "Low Stock" -> Color(0xFFEF6C00)
                                else -> Color(0xFFC62828)
                            }

                            Box(
                                modifier = Modifier
                                    .background(statusBgColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    product.stockStatus,
                                    color = statusTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Description
                item {
                    Column {
                        Text("Decryption & Scribe", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            lineHeight = 22.sp,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }

                // Archival specifications
                item {
                    Column {
                        Text("Scroll Specifications", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Parse the specsJson
                        val specsMap = remember(product.specsJson) {
                            product.specsJson.split(",").associate {
                                val split = it.split(":")
                                if (split.size == 2) split[0].trim() to split[1].trim() else "" to ""
                            }.filterKeys { it.isNotEmpty() }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            specsMap.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            if (product.pages > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pages count", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("${product.pages} sheets", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            if (product.publisher.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Official publisher", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(product.publisher, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // User reviews and star breakdown
                item {
                    Column {
                        Text("Scholarly Reviews", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            mockReviewsList.forEach { rev ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .background(Color(rev.avatarColor), RoundedCornerShape(15.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        rev.userName.take(1),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Text(rev.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        M3RatingBar(rating = rev.rating.toFloat(), starSize = 12.dp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(rev.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Similar records recommendations
                if (relatedProducts.isNotEmpty()) {
                    item {
                        Column {
                            Text("Related Astrolabes", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(relatedProducts) { related ->
                                    val faved = wishlistItems.any { it.productId == related.id }
                                    ProductCard(
                                        product = related,
                                        onProductClick = onProductSelected,
                                        onAddToCard = { viewModel.addToCart(related.id) },
                                        onWishlistToggle = { viewModel.toggleWishlist(related.id) },
                                        isWishlisted = faved,
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating bottom actions dock
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isInCart = viewModel.cart.collectAsState().value.any { it.productId == product.id }
                    
                    Button(
                        onClick = {
                            if (isInCart) {
                                viewModel.updateCartQuantity(product.id, 0)
                            } else {
                                viewModel.addToCart(product.id)
                            }
                        },
                        modifier = Modifier
                            .testTag("detail_add_to_cart_btn")
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInCart) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isInCart) "Evict from Trolley" else "Place in Trolley",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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
fun WishlistScreen(
    viewModel: LibraryViewModel,
    onProductSelected: (Int) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val wishlistItems by viewModel.wishlist.collectAsState()
    val productsList by viewModel.products.collectAsState()

    val favProducts = productsList.filter { prod -> wishlistItems.any { it.productId == prod.id } }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Reserved Astral Wishlist", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (favProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your wishlist vault is empty.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
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
                    items(favProducts) { prod ->
                        ProductCard(
                            product = prod,
                            onProductClick = onProductSelected,
                            onAddToCard = { viewModel.addToCart(prod.id) },
                            onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                            isWishlisted = true
                        )
                    }
                }
            }
        }
    }
}
