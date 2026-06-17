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
import com.example.ui.theme.*
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val ordersList by viewModel.orders.collectAsState()

    var activeTab by remember { mutableStateOf("Analytics") }

     CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Librarian Vault: Executive Control", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Horizontal Tab selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("Analytics", "Catalog", "Logistics", "Banners")
                tabs.forEach { tab ->
                    val isSel = activeTab == tab
                    Box(
                        modifier = Modifier
                            .testTag("admin_tab_$tab")
                            .clickable { activeTab = tab }
                            .background(
                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body selector
            when (activeTab) {
                "Analytics" -> AdminAnalyticsPanel(productsList, ordersList)
                "Catalog" -> AdminCatalogPanel(productsList, viewModel)
                "Logistics" -> AdminLogisticsPanel(ordersList, viewModel)
                "Banners" -> AdminBannersPanel(viewModel)
            }
        }
    }
}

@Composable
fun AdminAnalyticsPanel(products: List<Product>, orders: List<Order>) {
    val totalRevenue = orders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount }
    val pendingShipments = orders.count { it.status != "Delivered" && it.status != "Cancelled" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("General Registries", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Stats Matrix Cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Orders", fontSize = 11.sp)
                        Text("${orders.size}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Revenues", fontSize = 11.sp)
                        Text("₹${totalRevenue.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Awaiting Transit", fontSize = 11.sp)
                        Text("$pendingShipments", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Product Count", fontSize = 11.sp)
                        Text("${products.size}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DayPrimary)
                    }
                }
            }
        }

        // Dummy Sales Graphic
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Sales Graph (June 2026)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Display simple bar representations of sales
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val values = listOf(0.2f, 0.4f, 0.3f, 0.7f, 0.5f, 0.9f, 0.8f)
                            values.forEach { scale ->
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(scale)
                                        .clip(RoundedCornerShape(t9 = 4.dp, t10 = 4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Helper for drawing rounded top-only bars
private fun RoundedCornerShape(t9: androidx.compose.ui.unit.Dp, t10: androidx.compose.ui.unit.Dp): RoundedCornerShape {
    return RoundedCornerShape(topStart = t9, topEnd = t10)
}

@Composable
fun AdminCatalogPanel(products: List<Product>, viewModel: LibraryViewModel) {
    var showAddProductForm by remember { mutableStateOf(false) }

    // Add product state variables
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Books") }
    var subcategory by remember { mutableStateOf("Novels") }
    var price by remember { mutableStateOf("") }
    var discPrice by remember { mutableStateOf("") }
    var stockCount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Catalogue Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { showAddProductForm = !showAddProductForm },
                    modifier = Modifier.testTag("admin_add_product_toggle_btn")
                ) {
                    Text(if (showAddProductForm) "Collapse" else "+ Inscribe New")
                }
            }
        }

        // Add Product Form Dropdown
        if (showAddProductForm) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Inscribe Brand/Book Parameters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author / Brand") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description summary") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = subcategory, onValueChange = { subcategory = it }, label = { Text("Subcategory") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = discPrice, onValueChange = { discPrice = it }, label = { Text("Discounted (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        OutlinedTextField(value = stockCount, onValueChange = { stockCount = it }, label = { Text("Core stock count") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                        Button(
                            onClick = {
                                val p = price.toDoubleOrNull() ?: 0.0
                                val dp = discPrice.toDoubleOrNull() ?: 0.0
                                val sc = stockCount.toIntOrNull() ?: 0
                                if (title.isNotEmpty() && p > 0.0) {
                                    viewModel.adminAddProduct(title, author, desc, category, subcategory, p, dp, 5.0f, 1, sc)
                                    // Reset fields
                                    title = ""; author = ""; desc = ""; price = ""; discPrice = ""; stockCount = ""
                                    showAddProductForm = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_save_product_btn")
                        ) {
                            Text("Inscribe into Records")
                        }
                    }
                }
            }
        }

        // Catalog List
        items(products) { prod ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Category: ${prod.category} | Stock: ${prod.stockCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = { viewModel.adminDeleteProduct(prod) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLogisticsPanel(orders: List<Order>, viewModel: LibraryViewModel) {
    val logisticStages = listOf("Order Placed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Active Shipping Dispatches", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (orders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No transcripts ordered yet.")
                }
            }
        } else {
            items(orders) { order ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Order ID: #${order.orderId}", fontWeight = FontWeight.Bold)
                            Text("Mode: ${order.paymentMethod}", fontSize = 11.sp)
                        }
                        Text("Payable: ₹${order.totalAmount.toInt()}", fontSize = 13.sp)
                        Text("Status: ${order.status}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stage forward triggers
                        val currIndex = logisticStages.indexOf(order.status)
                        if (currIndex >= 0 && currIndex < logisticStages.size - 1) {
                            val nextStage = logisticStages[currIndex + 1]
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(order.orderId, nextStage) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_forward_btn_${order.orderId}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Dispatch Stage Forward -> $nextStage")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBannersPanel(viewModel: LibraryViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Magical Broadcaster Systems", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Live Broadcast Banner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Active: Monsoon Reading Festival - Flat ₹100 Off coupon LUNARA100")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Promotional Coupon Registers", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    viewModel.repository.availableCoupons.forEach { cop ->
                        Text("• ${cop.code}: ${cop.description}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
