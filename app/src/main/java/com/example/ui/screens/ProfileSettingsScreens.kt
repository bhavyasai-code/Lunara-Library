package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.LibraryViewModel
import com.example.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LibraryViewModel,
    onOrdersNavigate: () -> Unit,
    onSettingsNavigate: () -> Unit,
    onThemeNavigate: () -> Unit,
    onLogoutNavigate: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val ordersList by viewModel.orders.collectAsState()
    val wishlistItems by viewModel.wishlist.collectAsState()

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Scholar Cabin", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Large Avatar details block
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(40.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (userSession.isLoggedIn) userSession.name.take(1) else "S",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (userSession.isLoggedIn) userSession.name else "Anonymous Scholar",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = if (userSession.isLoggedIn) userSession.email else "No enrolled scrolls detected",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Cabinet statistical metrics
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${ordersList.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text("Orders Placed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${wishlistItems.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text("Wishlist Vault", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // General options list
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Column {
                            ProfileOptionItem(Icons.Default.Book, "Archival Order History", onOrdersNavigate)
                            Divider()
                            ProfileOptionItem(Icons.Default.LocationOn, "Logistical Addresses", onSettingsNavigate)
                            Divider()
                            ProfileOptionItem(Icons.Default.Palette, "Immersive Theme Settings", onThemeNavigate)
                        }
                    }
                }

                // Log out action
                item {
                    Button(
                        onClick = {
                            viewModel.signOutUser()
                            onLogoutNavigate()
                        },
                        modifier = Modifier
                            .testTag("logout_btn")
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Vacate Cabin (Log Out)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val addressList by viewModel.addresses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog inputs
    var dName by remember { mutableStateOf("") }
    var dPhone by remember { mutableStateOf("") }
    var dStreet by remember { mutableStateOf("") }
    var dCity by remember { mutableStateOf("") }
    var dState by remember { mutableStateOf("") }
    var dPincode by remember { mutableStateOf("") }

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Logistical Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // Address Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Registered Dispatch Addresses", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_address_dialog_btn")
                        ) {
                            Text("+ Add")
                        }
                    }
                }

                if (addressList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No shipping addresses on record.")
                        }
                    }
                } else {
                    items(addressList) { addr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(addr.name, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { viewModel.deleteAddress(addr) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(addr.streetAddress, fontSize = 13.sp)
                                Text("${addr.city}, ${addr.state} - ${addr.pincode}", fontSize = 13.sp)
                                Text("Contact: ${addr.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Pop-up registration dialog for addresses
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Assemble Delivery Location") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = dName, onValueChange = { dName = it }, label = { Text("Recipient Name") })
                            OutlinedTextField(value = dPhone, onValueChange = { dPhone = it }, label = { Text("Contact Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                            OutlinedTextField(value = dStreet, onValueChange = { dStreet = it }, label = { Text("Street, Apartment") })
                            OutlinedTextField(value = dCity, onValueChange = { dCity = it }, label = { Text("City") })
                            OutlinedTextField(value = dState, onValueChange = { dState = it }, label = { Text("State") })
                            OutlinedTextField(value = dPincode, onValueChange = { dPincode = it }, label = { Text("Pincode (ZIP)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (dName.isNotEmpty() && dPhone.isNotEmpty() && dStreet.isNotEmpty() && dPincode.isNotEmpty()) {
                                    viewModel.addAddress(dName, dPhone, dStreet, dCity, dState, dPincode, true)
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.testTag("confirm_address_dialog_btn")
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) { Text("Dismiss") }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()

    CelestialBackground(themeMode = themeMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Atmospheric Controls") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Atmosphere Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "\"The library never changes. Only the time of day changes.\" Toggle manual suns or nocturnal lantern alignments below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sunlit Option
                ThemeSelectionRow(
                    title = "The Sunlit Library (Day Mode)",
                    desc = "Warm sunlight fills the halls with golden wooden tones.",
                    isSelected = themeMode == ThemeMode.DAY,
                    onClick = { viewModel.setThemeMode(ThemeMode.DAY) },
                    iconElement = { Icon(Icons.Default.LightMode, contentDescription = null, tint = DayPrimary) },
                    testTag = "theme_day_opt"
                )

                // Moonlit Option
                ThemeSelectionRow(
                    title = "The Moonlit Library (Night Mode)",
                    desc = "Shadowed blue shelves illuminated by glowing lanterns.",
                    isSelected = themeMode == ThemeMode.NIGHT,
                    onClick = { viewModel.setThemeMode(ThemeMode.NIGHT) },
                    iconElement = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF00E5FF)) },
                    testTag = "theme_night_opt"
                )

                // Auto Option
                ThemeSelectionRow(
                    title = "Cosmic Synchronicity (Auto Mode)",
                    desc = "Let the library cycles match your device context naturally.",
                    isSelected = themeMode == ThemeMode.AUTO,
                    onClick = { viewModel.setThemeMode(ThemeMode.AUTO) },
                    iconElement = { Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Magenta) },
                    testTag = "theme_auto_opt"
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionRow(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconElement: @Composable () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            iconElement()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}
