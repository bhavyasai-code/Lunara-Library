package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Product
import com.example.ui.theme.*
import com.example.viewmodel.ThemeMode
import kotlin.random.Random

@Composable
fun CelestialBackground(
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
    }

    // Cozy Parchment gradient vs Midnight Velvet gradient
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030712), // Cosmic Midnight
                Color(0xFF0F172A), // Shadowed Hall Navy
                Color(0xFF1E293B)  // Deep Stone Library Blue
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFDF9F3), // Sunglow Parchment
                Color(0xFFF5EFE6), // Soft Library Oak
                Color(0xFFEFE8DA)  // Warm Antique Binding
            )
        )
    }

    // Creating beautiful floating dust particles / stars
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    // Smooth looping state for particles and twinkling
    val animState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim"
    )

    // Pre-calculate coordinates for 30 ambient particles
    val particles = remember {
        List(30) {
            Triple(
                Random.nextFloat(), // X ratio
                Random.nextFloat(), // Y ratio
                Random.nextFloat() * 6f + 2f // size in pixels
            )
        }
    }

    // Static random positions for 20 background stars
    val backgroundStars = remember {
        List(20) {
            Pair(Random.nextFloat(), Random.nextFloat())
        }
    }

    Box(modifier = modifier.fillMaxSize().background(gradientBrush)) {
        // Draw the exact same architecture, but with changing atmospheric light and star twinkle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Static Background Stars (Twinkling, NIGHT Mode ONLY)
            if (isDark) {
                backgroundStars.forEachIndexed { idx, star ->
                    // Twinkle offset calculation
                    val factor = kotlin.math.sin(animState.value * 2 * Math.PI + idx).toFloat()
                    val starAlpha = (0.3f + 0.5f * factor).coerceIn(0.1f, 0.9f)
                    
                    drawCircle(
                        color = Color(0xFFE2E8F0).copy(alpha = starAlpha),
                        radius = 2.dp.toPx(),
                        center = Offset(star.first * w, star.second * h)
                    )
                }
            }

            // 2. Draw Identical Arched Library Windows (Architectural structure never changes)
            val windowLineColor = if (isDark) {
                Color(0xFF38BDF8).copy(alpha = 0.08f) // Silver-blue glass outlines
            } else {
                Color(0xFF8D6E63).copy(alpha = 0.12f) // Warm oak glass outlines
            }
            
            // Draw window frames on the right (imaginary windows where the sun/moon beams come from)
            val windowX1 = w * 0.70f
            val windowWidth = w * 0.20f
            val windowY = h * 0.06f
            val windowHeight = h * 0.25f

            // Arched top window 1
            drawRoundRect(
                color = windowLineColor,
                topLeft = Offset(windowX1, windowY),
                size = androidx.compose.ui.geometry.Size(windowWidth, windowHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(windowWidth / 2, windowWidth / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            // Window 1 pane divides
            drawLine(windowLineColor, Offset(windowX1 + windowWidth/2, windowY), Offset(windowX1 + windowWidth/2, windowY + windowHeight), strokeWidth = 1.dp.toPx())
            drawLine(windowLineColor, Offset(windowX1, windowY + windowHeight/2), Offset(windowX1 + windowWidth, windowY + windowHeight/2), strokeWidth = 1.dp.toPx())

            // Arched top window 2 (positioned slightly left for double windows)
            val windowX2 = w * 0.45f
            drawRoundRect(
                color = windowLineColor,
                topLeft = Offset(windowX2, windowY),
                size = androidx.compose.ui.geometry.Size(windowWidth, windowHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(windowWidth / 2, windowWidth / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            // Window 2 pane divides
            drawLine(windowLineColor, Offset(windowX2 + windowWidth/2, windowY), Offset(windowX2 + windowWidth/2, windowY + windowHeight), strokeWidth = 1.dp.toPx())
            drawLine(windowLineColor, Offset(windowX2, windowY + windowHeight/2), Offset(windowX2 + windowWidth, windowY + windowHeight/2), strokeWidth = 1.dp.toPx())


            // 3. Draw Beautiful Diagonal Volumetric Light Beams (Vaporous shafts from top-right down to left)
            // The placement is identical between Day & Night, only the colors and shadows shift
            val beamColor = if (isDark) {
                Color(0xFFBDD5EA).copy(alpha = 0.07f) // Moonbeam silver-blue
            } else {
                Color(0xFFFFF3CD).copy(alpha = 0.12f) // Golden warm solar beams
            }

            // High-fidelity diagonal light beam path (from window side towards bottom left)
            val path1 = androidx.compose.ui.graphics.Path().apply {
                moveTo(windowX1 + windowWidth/2, windowY)
                lineTo(windowX1 + windowWidth * 1.5f, windowY)
                lineTo(windowX1 - w * 0.4f, h)
                lineTo(windowX1 - w * 0.7f, h)
                close()
            }
            drawPath(path1, color = beamColor)

            val path2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(windowX2 + windowWidth/2, windowY)
                lineTo(windowX2 + windowWidth * 1.5f, windowY)
                lineTo(windowX2 - w * 0.4f, h)
                lineTo(windowX2 - w * 0.7f, h)
                close()
            }
            drawPath(path2, color = beamColor)


            // 4. Draw Atmospheric Floating Particles (Rising magical dust/sparks)
            particles.forEachIndexed { i, particle ->
                val xRatio = particle.first
                // Rise upward continuously over the scroll animation
                val yRatio = (particle.second - (animState.value * 0.12f) + 1f) % 1f
                val sizePx = particle.third

                val x = xRatio * w
                val y = yRatio * h

                val color = if (isDark) {
                    // Night: Magic cyan/teal floating lanterns or lunar particles
                    val alpha = (0.2f + 0.4f * kotlin.math.sin(animState.value * 2 * Math.PI + i)).toFloat().coerceIn(0.1f, 0.7f)
                    Color(0xFF00E5FF).copy(alpha = alpha) // Enchanted glowing Cyan
                } else {
                    // Day: Sunlit warm gold dust motes slowly suspended in the air
                    val alpha = (0.2f + 0.3f * kotlin.math.cos(animState.value * 2 * Math.PI + i)).toFloat().coerceIn(0.1f, 0.6f)
                    Color(0xFFD4AF37).copy(alpha = alpha) // Warm Gold dust
                }

                drawCircle(
                    color = color,
                    radius = sizePx,
                    center = Offset(x, y)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Int) -> Unit,
    onAddToCard: () -> Unit,
    onWishlistToggle: () -> Unit,
    isWishlisted: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background != DayBackground
    
    // Theme-dependent decorative color properties
    val spineBrush = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF080E1E), Color(0xFF1E293B), Color(0xFF080E1E)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF3E2723), Color(0xFF6D4C41), Color(0xFF3E2723)))
    }
    
    val spineRibColor = if (isDark) Color(0xFF818CF8).copy(alpha = 0.5f) else Color(0xFFD4AF37).copy(alpha = 0.6f)
    val cardBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFD7CCC8)
    val filigreeColor = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.3f) else Color(0xFFD4AF37).copy(alpha = 0.4f)

    Card(
        modifier = modifier
            .testTag("product_card_${product.id}")
            .fillMaxWidth()
            .clickable { onProductClick(product.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFBF9F4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // 1. Decorative Vintage Book Spine
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight()
                        .height(295.dp) // align with overall book card height approx
                        .background(spineBrush)
                ) {
                    // Vintage horizontal ribs on the book spine
                    Column(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(spineRibColor)
                            )
                        }
                    }
                }

                // 2. Main Book Interface
                Column {
                    // Book Cover Picture Frame with Filigree Corners
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFEFE8DA).copy(alpha = 0.4f))
                    ) {
                        val context = LocalContext.current
                        val imgRes = when (product.imageUrl) {
                            "book_celestial" -> R.drawable.img_app_logo
                            "stat_journal" -> R.drawable.img_library_banner
                            "stat_pen_amber" -> R.drawable.img_app_logo
                            "book_upsc" -> R.drawable.img_library_banner
                            else -> R.drawable.img_app_logo
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imgRes)
                                .crossfade(true)
                                .build(),
                            contentDescription = product.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Fit
                        )

                        // Gilded thin inner border mimicking historical framing
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .background(Color.Transparent)
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .background(Color.Transparent)
                        ) {
                            // Fancy ornamental corner details
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizePx = 10.dp.toPx()
                                // Top-Left
                                drawLine(filigreeColor, Offset(0f, 0f), Offset(sizePx, 0f), strokeWidth = 1.5.dp.toPx())
                                drawLine(filigreeColor, Offset(0f, 0f), Offset(0f, sizePx), strokeWidth = 1.5.dp.toPx())
                                // Top-Right
                                drawLine(filigreeColor, Offset(size.width, 0f), Offset(size.width - sizePx, 0f), strokeWidth = 1.5.dp.toPx())
                                drawLine(filigreeColor, Offset(size.width, 0f), Offset(size.width, sizePx), strokeWidth = 1.5.dp.toPx())
                                // Bottom-Left
                                drawLine(filigreeColor, Offset(0f, size.height), Offset(sizePx, size.height), strokeWidth = 1.5.dp.toPx())
                                drawLine(filigreeColor, Offset(0f, size.height), Offset(0f, size.height - sizePx), strokeWidth = 1.5.dp.toPx())
                                // Bottom-Right
                                drawLine(filigreeColor, Offset(size.width, size.height), Offset(size.width - sizePx, size.height), strokeWidth = 1.5.dp.toPx())
                                drawLine(filigreeColor, Offset(size.width, size.height), Offset(size.width, size.height - sizePx), strokeWidth = 1.5.dp.toPx())
                            }
                        }

                        // Stock wax seal
                        val stockBgColor = when (product.stockStatus) {
                            "In Stock" -> Color(0xFF2E7D32)
                            "Low Stock" -> Color(0xFFEF6C00)
                            else -> Color(0xFFC62828)
                        }

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .background(stockBgColor.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = product.stockStatus,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Metadata details representing antique spine text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = (if (product.brand.isNotEmpty()) product.brand else product.author).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF8D6E63),
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = product.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Serif,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rating Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = if (isDark) Color(0xFF00E5FF) else DayPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${product.rating}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "(${product.reviewsCount})",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pricing and Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val originalPrice = product.price
                                val discountedPrice = product.discountPrice
                                if (discountedPrice > 0f) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹${discountedPrice.toInt()}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = if (isDark) Color(0xFF00E5FF) else DaySecondary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "₹${originalPrice.toInt()}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "₹${originalPrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Compact Add To Cart Circular Button styled as an ornate medallion
                            Button(
                                onClick = onAddToCard,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .testTag("btn_cart_add_${product.id}")
                                    .size(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.25f) else DayPrimary,
                                    contentColor = if (isDark) Color(0xFF00E5FF) else DayOnPrimary
                                )
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Wishlist Toggle Top Right
            IconButton(
                onClick = onWishlistToggle,
                modifier = Modifier
                    .testTag("wish_toggle_${product.id}")
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        RoundedCornerShape(30.dp)
                    )
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorited",
                    tint = if (isWishlisted) {
                        if (isDark) Color(0xFF00E5FF) else DayPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun M3RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    tint: Color = DayPrimary
) {
    Row(modifier = modifier) {
        val fullStars = rating.toInt()
        val hasHalf = (rating - fullStars) >= 0.5f

        for (i in 1..5) {
            val icon = when {
                i <= fullStars -> Icons.Filled.Star
                i == fullStars + 1 && hasHalf -> Icons.Filled.Star // simplify half-star as filled
                else -> Icons.Outlined.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(starSize)
            )
        }
    }
}
