package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.LibraryDatabase
import com.example.data.model.*
import com.example.data.repository.LibraryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ThemeMode {
    AUTO, DAY, NIGHT
}

data class UserSession(
    val email: String,
    val name: String,
    val phone: String = "",
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        LibraryDatabase::class.java,
        "lunara_library_database"
    ).fallbackToDestructiveMigration().build()

    val repository = LibraryRepository(db)

    // Dynamic Theme state
    private val _themeMode = MutableStateFlow(ThemeMode.AUTO)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Authentication session state
    private val _userSession = MutableStateFlow(UserSession("", "", "", false, false))
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val minPrice = MutableStateFlow(0f)
    val maxPrice = MutableStateFlow(2000f)
    val minRating = MutableStateFlow(0f)
    val onlyInStock = MutableStateFlow(false)

    // Applied coupon
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    // Active order tracking selection
    private val _selectedOrderId = MutableStateFlow<String?>(null)
    val selectedOrderId: StateFlow<String?> = _selectedOrderId.asStateFlow()

    // Flow maps
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cart: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlist: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<UserAddress>> = repository.allAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        products, searchQuery, selectedCategory, minPrice, maxPrice, minRating, onlyInStock
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val prodList = flows[0] as List<Product>
        val query = flows[1] as String
        val cat = flows[2] as String
        val minP = flows[3] as Float
        val maxP = flows[4] as Float
        val rating = flows[5] as Float
        val stockOnly = flows[6] as Boolean

        prodList.filter { product ->
            val matchQuery = query.isEmpty() || product.title.contains(query, ignoreCase = true) || 
                             product.author.contains(query, ignoreCase = true) || product.category.contains(query, ignoreCase = true)
            val matchCat = cat == "All" || product.category.equals(cat, ignoreCase = true) || product.subcategory.equals(cat, ignoreCase = true)
            val finalPrice = if (product.discountPrice > 0) product.discountPrice else product.price
            val matchPrice = finalPrice >= minP && finalPrice <= maxP
            val matchRating = product.rating >= rating
            val matchStock = !stockOnly || product.stockStatus == "In Stock" || product.stockCount > 0
            
            matchQuery && matchCat && matchPrice && matchRating && matchStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    // Theme control
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    // Checkout Calculations helper
    fun getCartSummary(cartItems: List<CartItem>, productList: List<Product>): Triple<Double, Double, Double> {
        var subtotal = 0.0
        cartItems.forEach { item ->
            val product = productList.find { it.id == item.productId }
            if (product != null && !item.isSavedForLater) {
                val priceToUse = if (product.discountPrice > 0) product.discountPrice else product.price
                subtotal += (priceToUse * item.quantity)
            }
        }

        var couponDiscount = 0.0
        val promo = _appliedCoupon.value
        if (promo != null && subtotal >= promo.minOrderValue) {
            couponDiscount = promo.discountAmount
        } else if (promo != null) {
            // Coupon no longer valid because subtotal fell below min order
            _appliedCoupon.value = null
        }

        val shipping = if (subtotal > 0.0 && subtotal < 600.0) 49.0 else 0.0
        val total = (subtotal - couponDiscount + shipping).coerceAtLeast(0.0)

        // Triple of (Subtotal, Discount, GrandTotal) - where GrandTotal includes shipping
        return Triple(subtotal, couponDiscount, total)
    }

    // Coupon actions
    fun applyCoupon(code: String): Boolean {
        val coupon = repository.availableCoupons.find { it.code.trim().equals(code.trim(), ignoreCase = true) }
        return if (coupon != null) {
            _appliedCoupon.value = coupon
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // Auth actions
    fun signInUser(email: String, name: String, phone: String) {
        _userSession.value = UserSession(
            email = email,
            name = name,
            phone = phone,
            isLoggedIn = true,
            isAdmin = email.trim().equals("admin@lunara.com", ignoreCase = true)
        )
    }

    fun signOutUser() {
        _userSession.value = UserSession("", "", "", false, false)
        _appliedCoupon.value = null
        _selectedOrderId.value = null
    }

    // Cart integrations
    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(productId, quantity)
        }
    }

    fun updateCartQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun toggleCartSaveState(productId: Int) {
        viewModelScope.launch {
            repository.toggleSaveForLater(productId)
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    // Wishlist integrations
    fun toggleWishlist(productId: Int) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // Order checkout placement
    fun checkout(paymentMethod: String, address: UserAddress, items: List<CartItem>, subtotal: Double, total: Double) {
        viewModelScope.launch {
            val orderId = "LNL-" + (100000..999999).random().toString()
            val itemsJson = items.joinToString(",") { "${it.productId}:${it.quantity}" }
            
            val order = repository.placeOrder(
                orderId = orderId,
                totalAmount = total,
                paymentMethod = paymentMethod,
                address = "${address.name}, ${address.streetAddress}, ${address.city}, ${address.state} - ${address.pincode}",
                itemsJson = itemsJson
            )
            _selectedOrderId.value = order.orderId
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "Cancelled", "You have cancelled this magical order. Refunding credentials.")
        }
    }

    // Address integrations
    fun addAddress(name: String, phone: String, street: String, city: String, state: String, pincode: String, isDefault: Boolean) {
        viewModelScope.launch {
            repository.addAddress(
                UserAddress(
                    name = name,
                    phone = phone,
                    streetAddress = street,
                    city = city,
                    state = state,
                    pincode = pincode,
                    isDefault = isDefault
                )
            )
        }
    }

    fun deleteAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.removeAddress(address)
        }
    }

    // Dynamic Tracking Selection
    fun selectOrderForTracking(orderId: String) {
        _selectedOrderId.value = orderId
    }

    // Admin Panel direct hooks
    fun adminAddProduct(
        title: String, author: String, desc: String, cat: String, subcat: String,
        price: Double, dPrice: Double, rating: Float, count: Int, stock: Int
    ) {
        viewModelScope.launch {
            val product = Product(
                title = title,
                author = author,
                description = desc,
                category = cat,
                subcategory = subcat,
                price = price,
                discountPrice = dPrice,
                rating = rating,
                reviewsCount = count,
                imageUrl = "book_generic",
                stockStatus = if (stock > 0) "In Stock" else "Out of Stock",
                stockCount = stock,
                isNewArrival = true
            )
            repository.addProduct(product)
            repository.addNotification(
                title = "Library Expanding! 📚",
                message = "\"$title\" was added into our public catalogue archives. Browse now!",
                type = "Stock"
            )
        }
    }

    fun adminUpdateOrderStatus(orderId: String, nextStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }

    fun adminDeleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}
