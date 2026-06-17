package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserAddress
import com.example.viewmodel.LibraryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: LibraryViewModel,
    onCheckoutNavigate: () -> Unit,
    onProductSelected: (Int) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val cartItems by viewModel.cart.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val coupon by viewModel.appliedCoupon.collectAsState()

    val activeCart = cartItems.filter { !it.isSavedForLater }
    val savedCart = cartItems.filter { it.isSavedForLater }

    // Calc summaries
    val (subtotal, discount, total) = viewModel.getCartSummary(cartItems, productsList)
    val shipping = if (subtotal > 0f && subtotal < 600f) 49.0 else 0.0

    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf("") }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Celestial Cart Trolley", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your shopping trolley has floated away empty.", fontSize = 14.sp)
                    }
                }
                return@CelestialBackground
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Active Cart section
                if (activeCart.isNotEmpty()) {
                    item {
                        Text("Active Items (${activeCart.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    items(activeCart) { item ->
                        val product = productsList.find { it.id == item.productId }
                        if (product != null) {
                            CartProductRow(
                                product = product,
                                quantity = item.quantity,
                                onInc = { viewModel.updateCartQuantity(product.id, item.quantity + 1) },
                                onDec = { viewModel.updateCartQuantity(product.id, item.quantity - 1) },
                                onSaveLater = { viewModel.toggleCartSaveState(product.id) },
                                onRemove = { viewModel.removeFromCart(product.id) },
                                onProductSelected = onProductSelected
                            )
                        }
                    }
                }

                // Saved For Later Items section
                if (savedCart.isNotEmpty()) {
                    item {
                        Text("Reserved Stashed Shelf (${savedCart.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    items(savedCart) { item ->
                        val product = productsList.find { it.id == item.productId }
                        if (product != null) {
                            SavedLaterRow(
                                product = product,
                                onMoveToCart = { viewModel.toggleCartSaveState(product.id) },
                                onRemove = { viewModel.removeFromCart(product.id) },
                                onProductSelected = onProductSelected
                            )
                        }
                    }
                }

                if (activeCart.isNotEmpty()) {
                    // Promo Apply Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Voucher Coupons", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it; couponError = "" },
                                        placeholder = { Text("Enter Code: e.g. LUNARA100") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("promo_input_field"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            if (viewModel.applyCoupon(couponInput)) {
                                                couponError = ""
                                            } else {
                                                couponError = "Invalid celestial manual voucher code."
                                            }
                                        },
                                        modifier = Modifier.testTag("apply_coupon_btn"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }

                                if (coupon != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Applied: ${coupon!!.code} (-₹${coupon!!.discountAmount.toInt()})",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        IconButton(onClick = { viewModel.removeCoupon() }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                if (couponError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(couponError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Cost sheet breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Cost Decryption Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Divider()
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Items Subtotal", fontSize = 13.sp)
                                    Text("₹${subtotal.toInt()}", fontSize = 13.sp)
                                }
                                if (discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Voucher discount", fontSize = 13.sp, color = Color(0xFF2E7D32))
                                        Text("- ₹${discount.toInt()}", fontSize = 13.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Shipping logistics delivery", fontSize = 13.sp)
                                    Text(if (shipping > 0) "₹${shipping.toInt()}" else "Free Delivery", fontSize = 13.sp)
                                }
                                Divider()
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("₹${total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom CTA Trigger Row
            if (activeCart.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = onCheckoutNavigate,
                            modifier = Modifier
                                .testTag("checkout_trigger_btn")
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Proceed to Dispatch (₹${total.toInt()})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartProductRow(
    product: Product,
    quantity: Int,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onSaveLater: () -> Unit,
    onRemove: () -> Unit,
    onProductSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Title & controls
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onProductSelected(product.id) }
                )
                Text(
                    "by ${product.author}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Pricing row
                val price = if (product.discountPrice > 0) product.discountPrice else product.price
                Text("₹${price.toInt()} per unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Stash Later",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onSaveLater() }
                    )
                    Text(
                        "Evict",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onRemove() }
                    )
                }
            }

            // Quantity adjust controllers
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDec, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
                }
                Text("$quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onInc, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SavedLaterRow(
    product: Product,
    onMoveToCart: () -> Unit,
    onRemove: () -> Unit,
    onProductSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onProductSelected(product.id) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Restore to Trolley",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable { onMoveToCart() }
                    )
                    Text(
                        "Discard",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onRemove() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: LibraryViewModel,
    onPaymentNavigate: () -> Unit,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val addressList by viewModel.addresses.collectAsState()
    val cartList by viewModel.cart.collectAsState()
    val productsList by viewModel.products.collectAsState()

    val defaultAddress = addressList.find { it.isDefault } ?: addressList.firstOrNull()
    val (subtotal, discount, total) = viewModel.getCartSummary(cartList, productsList)

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Archival Dispatch Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Section 1: Default Shipping Address
                item {
                    Text("Shipping Destination", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (defaultAddress != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(defaultAddress.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(defaultAddress.streetAddress, fontSize = 13.sp)
                                Text("${defaultAddress.city}, ${defaultAddress.state} - ${defaultAddress.pincode}", fontSize = 13.sp)
                                Text("Phone: ${defaultAddress.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            } else {
                                Text("No address on record. Please set up addresses in settings.", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Section 2: Shipping Class
                item {
                    Text("Logistical Class", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = true, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Express Archival Delivery", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Dispatched under protective waxing seal within 2-4 days.", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section 3: Cost recapitulation
                item {
                    Text("Price Recapitulation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total Payable", fontWeight = FontWeight.Bold)
                                Text("₹${total.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            // Bottom CTA
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            if (defaultAddress != null) {
                                onPaymentNavigate()
                            }
                        },
                        modifier = Modifier
                            .testTag("submit_checkout_btn")
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = (defaultAddress != null),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed to Safe Payment", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: LibraryViewModel,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val cartList by viewModel.cart.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val addressList by viewModel.addresses.collectAsState()

    val defaultAddress = addressList.find { it.isDefault } ?: addressList.firstOrNull()
    val (subtotal, discount, total) = viewModel.getCartSummary(cartList, productsList)

    val payments = listOf("UPI (BHIM / GooglePay)", "Credit/Debit Cards (RuPay / Visa)", "Net Banking", "Cash on Delivery")
    var selectedPay by remember { mutableStateOf(payments[0]) }

    var upiId by remember { mutableStateOf("") }
    var processingPayment by remember { mutableStateOf(false) }

    LaunchedEffect(processingPayment) {
        if (processingPayment) {
            delay(2000)
            if (defaultAddress != null) {
                viewModel.checkout(selectedPay, defaultAddress, cartList, subtotal, total)
            }
            onPaymentSuccess()
        }
    }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Sanctum Pay Channel") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (processingPayment) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Interfacing cosmic bank nodes...", fontWeight = FontWeight.Bold)
                    }
                }
                return@CelestialBackground
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Select Payment Gateway", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(payments) { mode ->
                    val isSelected = selectedPay == mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPay = mode },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(mode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Dynamic Input dependent on UPI Choice
                if (selectedPay == "UPI (BHIM / GooglePay)") {
                    item {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Unified UPI ID (e.g. bhavya@okaxis)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upi_input_field"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Pay Trigger Bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = { processingPayment = true },
                        modifier = Modifier
                            .testTag("submit_pay_btn")
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = (selectedPay != "UPI (BHIM / GooglePay)" || upiId.isNotEmpty()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Complete Payment (₹${total.toInt()})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: LibraryViewModel,
    onTrackOrderNavigate: () -> Unit,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val ordersList by viewModel.orders.collectAsState()

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Archival Orders Scroll") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (ordersList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transcripts ordered yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ordersList) { order ->
                        Card(
                            modifier = Modifier
                                .testTag("order_history_card_${order.orderId}")
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order #${order.orderId}", fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Serif)
                                    Text(
                                        text = order.status,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.status == "Delivered") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Paid: ₹${order.totalAmount.toInt()}", fontSize = 13.sp)
                                    Button(
                                        onClick = {
                                            viewModel.selectOrderForTracking(order.orderId)
                                            onTrackOrderNavigate()
                                        },
                                        modifier = Modifier.testTag("track_btn_${order.orderId}"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Track progress")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TrackerMilestone(val status: String, val timestamp: Long?, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val trackingId by viewModel.selectedOrderId.collectAsState()
    val ordersList by viewModel.orders.collectAsState()

    val order = ordersList.find { it.orderId == trackingId }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Trace Conduit Status") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("track_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select an order to track.")
                }
                return@CelestialBackground
            }

            // Static milestones hierarchy
            val stages = listOf("Order Placed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered")
            val currentStageIndex = stages.indexOf(order.status).coerceAtLeast(0)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "AURA ID: #${order.orderId}",
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Shipping tracking pipeline active", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Interactive Milestones timeline
                items(stages.size) { index ->
                    val stage = stages[index]
                    val isActive = index <= currentStageIndex
                    val isPast = index < currentStageIndex

                    val milestoneColor = when {
                        isActive && index == currentStageIndex -> MaterialTheme.colorScheme.primary
                        isPast -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Drawing circles & connect lines
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(milestoneColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPast) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                } else {
                                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                                }
                            }

                            if (index < stages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(2.dp)
                                        .background(milestoneColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Description context
                        Column {
                            Text(
                                text = stage,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                            val descMsg = when (stage) {
                                "Order Placed" -> "Order registered successfully into archives."
                                "Processing" -> "Assembling items and reviewing inventories."
                                "Packed" -> "Crated with solar-seal protective paper tags."
                                "Shipped" -> "Dispatched on courier wings to transit terminals."
                                "Out for Delivery" -> "Our courier agent is delivering today inside destination."
                                "Delivered" -> "Resting safely inside study chambers. Enjoy!"
                                else -> "Pending logistical updates."
                            }
                            Text(
                                text = descMsg,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Close order cancellation
                if (order.status != "Delivered" && order.status != "Cancelled") {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelOrder(order.orderId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel Magical Order Registration")
                        }
                    }
                }
            }
        }
    }
}
