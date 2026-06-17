package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LibraryRepository(private val db: LibraryDatabase) {

    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val wishlistDao = db.wishlistDao()
    private val orderDao = db.orderDao()
    private val addressDao = db.addressDao()
    private val notificationDao = db.notificationDao()

    // Flows
    val allProducts: Flow<List<Product>> = productDao.getAllProductsFlow()
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItemsFlow()
    val wishlistItems: Flow<List<WishlistItem>> = wishlistDao.getWishlistItemsFlow()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrdersFlow()
    val allAddresses: Flow<List<UserAddress>> = addressDao.getAllAddressesFlow()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotificationsFlow()

    // Coupons
    val availableCoupons = listOf(
        Coupon("LUNARA100", 100.0, 500.0, "Get ₹100 off on orders above ₹500"),
        Coupon("MAGIC300", 300.0, 1500.0, "Get ₹300 off on magical orders above ₹1500"),
        Coupon("SOLARFEST", 150.0, 800.0, "Daylight celebration coupon code - Flat ₹150 off"),
        Coupon("MOONGLOW", 250.0, 1200.0, "Moonlit savings special - Flat ₹250 off")
    )

    // Pre-populate Products if empty
    suspend fun prepopulateDatabaseIfEmpty() {
        val products = productDao.getAllProductsFlow().first()
        if (products.isEmpty()) {
            val initialProducts = getMockProducts()
            productDao.insertProducts(initialProducts)
            
            // Insert dynamic initial setup notification
            notificationDao.insertNotification(
                NotificationItem(
                    title = "Welcome to Lunara Library!",
                    message = "Explore a magical repository of wisdom and exquisite tools. Enjoy automatic day and night atmospheric lighting.",
                    timestamp = System.currentTimeMillis() - 60000,
                    type = "System"
                )
            )

            // Insert initial default address
            addressDao.insertAddress(
                UserAddress(
                    name = "Bhavya Sai",
                    phone = "+91 9876543210",
                    streetAddress = "Flat 402, Lotus Residency, Gachibowli",
                    city = "Hyderabad",
                    state = "Telangana",
                    pincode = "500032",
                    isDefault = true
                )
            )
        }
    }

    // Product CRUD
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun addProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun clearProducts() = productDao.clearProducts()

    // Cart CRUD
    suspend fun addToCart(productId: Int, quantity: Int = 1) {
        val existing = cartItems.first().find { it.productId == productId }
        if (existing != null) {
            cartDao.insertCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insertCartItem(CartItem(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(CartItem(productId, 0))
        } else {
            val existing = cartItems.first().find { it.productId == productId }
            val saveState = existing?.isSavedForLater ?: false
            cartDao.insertCartItem(CartItem(productId, quantity, isSavedForLater = saveState))
        }
    }

    suspend fun toggleSaveForLater(productId: Int) {
        val existing = cartItems.first().find { it.productId == productId }
        if (existing != null) {
            cartDao.insertCartItem(existing.copy(isSavedForLater = !existing.isSavedForLater))
        }
    }

    suspend fun removeFromCart(productId: Int) {
        cartDao.deleteCartItem(CartItem(productId, 0))
    }

    suspend fun clearCart() = cartDao.clearCart()

    // Wishlist CRUD
    suspend fun toggleWishlist(productId: Int) {
        val list = wishlistItems.first()
        val exists = list.any { it.productId == productId }
        if (exists) {
            wishlistDao.deleteWishlist(WishlistItem(productId))
        } else {
            wishlistDao.insertWishlist(WishlistItem(productId))
        }
    }

    // Orders CRUD
    suspend fun placeOrder(
        orderId: String,
        totalAmount: Double,
        paymentMethod: String,
        address: String,
        itemsJson: String
    ): Order {
        val initialStatus = "Order Placed"
        val timestamp = System.currentTimeMillis()
        
        // Initial tracking updates
        val trackingJson = """
            [
                {"status": "Order Placed", "timestamp": $timestamp, "message": "Your magical package is registered and awaiting dispatch."},
                {"status": "Processing", "timestamp": ${timestamp + 15000}, "message": "Enchanted librarians are assembling your literary selections."}
            ]
        """.trimIndent()

        val order = Order(
            orderId = orderId,
            timestamp = timestamp,
            totalAmount = totalAmount,
            status = initialStatus,
            paymentMethod = paymentMethod,
            shippingAddress = address,
            itemsJson = itemsJson,
            trackingUpdatesJson = trackingJson
        )

        orderDao.insertOrder(order)
        clearCart()

        // Notification of order dispatch
        notificationDao.insertNotification(
            NotificationItem(
                title = "Order Applauded! 📜",
                message = "Your order $orderId has been placed in the Grand Archives. Track its transit progress anytime.",
                timestamp = System.currentTimeMillis(),
                type = "Order"
            )
        )

        return order
    }

    suspend fun updateOrderStatus(orderId: String, status: String, customMessage: String? = null) {
        val existing = orderDao.getOrderById(orderId)
        if (existing != null) {
            val currTime = System.currentTimeMillis()
            val msg = customMessage ?: when (status) {
                "Processing" -> "Scribes are prepping ink parameters."
                "Packed" -> "Sealed with official solar-wax crests."
                "Shipped" -> "Left the solar library gates; flying towards destination."
                "Out for Delivery" -> "Our familiar-courier is in your vicinity."
                "Delivered" -> "Delivered safely onto your study sanctuary. Happy Reading!"
                "Cancelled" -> "Transaction revoked in the magical registers."
                else -> "In transit."
            }

            // Append tracking update
            val cleanedTrackStr = existing.trackingUpdatesJson.trim().removeSuffix("]").removeSuffix(",")
            val newTrackJson = "$cleanedTrackStr, {\"status\": \"$status\", \"timestamp\": $currTime, \"message\": \"$msg\"}]"

            val updatedOrder = existing.copy(
                status = status,
                trackingUpdatesJson = newTrackJson
            )
            orderDao.insertOrder(updatedOrder)

            notificationDao.insertNotification(
                NotificationItem(
                    title = "Order Update: $status 🌟",
                    message = "Lunara Order $orderId has transitioned to $status. $msg",
                    timestamp = currTime,
                    type = "Order"
                )
            )
        }
    }

    // Address CRUD
    suspend fun addAddress(address: UserAddress) {
        addressDao.insertAndSetDefault(address)
    }

    suspend fun removeAddress(address: UserAddress) {
        addressDao.deleteAddress(address)
    }

    // Notifications CRUD
    suspend fun addNotification(title: String, message: String, type: String = "Promo") {
        notificationDao.insertNotification(
            NotificationItem(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type
            )
        )
    }

    suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotificationById(id)

    // Generator of immersive initial catalog
    private fun getMockProducts(): List<Product> {
        return listOf(
            Product(
                title = "The Celestial Almanac 2026",
                description = "An immersive, beautifully bound ledger detailing astronomical changes, constellations, and the delicate secrets of solar alignments in Indian architecture.",
                author = "Dr. Devendra Shastri",
                category = "Novels",
                subcategory = "Novels",
                price = 599.0,
                discountPrice = 449.0,
                rating = 4.9f,
                reviewsCount = 42,
                imageUrl = "book_celestial",
                stockStatus = "In Stock",
                stockCount = 12,
                isFeatured = true,
                isBestSeller = true,
                publisher = "Bharatiya Astral Press",
                pages = 320,
                specsJson = "Binding: Hardcover, Pages: 320, Language: English & Sanskrit"
            ),
            Product(
                title = "Enchanted Leaves Journal",
                description = "Handmade from 120gsm recycled plant fiber parchment paper. Features an intricate woodcut sun & moon cover engraving, complete with visual brass latch.",
                author = "Lunara Crafts",
                category = "Stationery",
                subcategory = "Journals",
                price = 899.0,
                discountPrice = 649.0,
                rating = 4.8f,
                reviewsCount = 128,
                imageUrl = "stat_journal",
                stockStatus = "In Stock",
                stockCount = 15,
                isFeatured = true,
                brand = "Lunara Studio",
                specsJson = "Paper Weight: 120gsm, Size: A5, Bound: Organic Linen Stitching"
            ),
            Product(
                title = "Solaris Fountain Pen - Amber Gold",
                description = "Crafted with dynamic brass elements, this medium-nib fountain pen stores ink inside an organic resin dome that shines elegantly under sunlight.",
                author = "Parker & Partners",
                category = "Stationery",
                subcategory = "Pens",
                price = 1499.0,
                discountPrice = 1199.0,
                rating = 4.7f,
                reviewsCount = 35,
                imageUrl = "stat_pen_amber",
                stockStatus = "Low Stock",
                stockCount = 4,
                isBestSeller = true,
                brand = "Aethelgard",
                specsJson = "Nib Size: Medium, Ink Support: Cartridge/Converter, Weight: 28 grams"
            ),
            Product(
                title = "UPSC IAS Master Guide",
                description = "The ultimate companion to cracking the Indian Civil Services exam. Contains complete history overlays, dynamic charts, and previous 10 years of solved question papers.",
                author = "Srikant Verma (Ex-IAS)",
                category = "Books",
                subcategory = "Competitive Exam Books",
                price = 1250.0,
                discountPrice = 949.0,
                rating = 4.6f,
                reviewsCount = 203,
                imageUrl = "book_upsc",
                stockStatus = "In Stock",
                stockCount = 50,
                isFeatured = false,
                isBestSeller = true,
                publisher = "Dharma Academy Books",
                pages = 880,
                specsJson = "Syllabus Year: 2026 Edition, Paper Class: Creamwood newsprint"
            ),
            Product(
                title = "Silver Moonlight Sketch Pencils",
                description = "Professional sketching kit spanning 12 levels of lead core densities (12B to 2H). Finished with a metallic lunar-dust matte grip that stays clean in long drawing sessions.",
                author = "Nataraj FineArts",
                category = "Stationery",
                subcategory = "Pencils",
                price = 350.0,
                discountPrice = 249.0,
                rating = 4.5f,
                reviewsCount = 76,
                imageUrl = "stat_pencils",
                stockStatus = "In Stock",
                stockCount = 22,
                brand = "Lunara Art",
                specsJson = "Pencil Count: 12 Pieces, Grade: Premium Lead, Sharpener: Included"
            ),
            Product(
                title = "The Forest of Starry Whispers",
                description = "A warm, magical children's adventure set in the deep woodlands of Western Ghats, where talking leopards and glowing birds guide a young girl to wisdom.",
                author = "Ananya Mukherjee",
                category = "Books",
                subcategory = "Children's Books",
                price = 450.0,
                discountPrice = 299.0,
                rating = 4.9f,
                reviewsCount = 48,
                imageUrl = "book_forest",
                stockStatus = "In Stock",
                stockCount = 18,
                isNewArrival = true,
                publisher = "Panchatantra Magic Press",
                pages = 180,
                specsJson = "Age Group: 6-12 Years, Illustrations: Hand-drawn watercolours"
            ),
            Product(
                title = "Vaidya Ayurvedic Botanical Guide",
                description = "Explore therapeutic herbs, remedies, and ancient botanical wisdom from sacred scripts, catalogued with beautiful Indian watercolor floral prints.",
                author = "Acharya K. Raghavan",
                category = "Books",
                subcategory = "Academic Books",
                price = 999.0,
                discountPrice = 799.0,
                rating = 4.8f,
                reviewsCount = 92,
                imageUrl = "book_botanical",
                stockStatus = "In Stock",
                stockCount = 10,
                publisher = "Sanskrit Heritage",
                pages = 412,
                specsJson = "Subclass: Ayurvedic Botany, Reference: Charaka Samhita Vol II"
            ),
            Product(
                title = "Astral Dream Calligraphy Ink Set",
                description = "A luxurious collection of 5 shimmering inks that glow softly under ultraviolet or warm night light. Colors include Royal Indigo, Sunfire Gold, Sage Mint, Lunar Silver, and Ochre.",
                author = "Atelier India",
                category = "Stationery",
                subcategory = "Art Supplies",
                price = 1100.0,
                discountPrice = 849.0,
                rating = 4.9f,
                reviewsCount = 29,
                imageUrl = "stat_ink",
                stockStatus = "In Stock",
                stockCount = 8,
                isNewArrival = true,
                brand = "Atelier Artisans",
                specsJson = "Volume: 20ml per bottle, Shimmer: Platinum flakes, Ink Type: Shellac-free water-based"
            ),
            Product(
                title = "Ancient Monuments of India",
                description = "A premium bimonthly architectural magazine taking readers deep inside the ancient astronomical libraries, observatories, and stone temples across Hampi, Konark, and Delhi.",
                author = "Archeological Heritage Inst.",
                category = "Books",
                subcategory = "Magazines",
                price = 200.0,
                discountPrice = 175.0,
                rating = 4.4f,
                reviewsCount = 18,
                imageUrl = "book_mag_monuments",
                stockStatus = "In Stock",
                stockCount = 60,
                publisher = "INTACH Publications",
                pages = 96,
                specsJson = "Paper: Premium Glossy 150gsm, Dimensions: 21 x 29.7 cm"
            ),
            Product(
                title = "Ethereal Brass Scale Ruler",
                description = "A beautifully functional solid brass 15cm geometric ruler with engraved astronomical coordinate carvings along its spine. Develops a warm bronze patina over years.",
                author = "Classic Deskware",
                category = "Stationery",
                subcategory = "Study Accessories",
                price = 499.0,
                discountPrice = 399.0,
                rating = 4.6f,
                reviewsCount = 67,
                imageUrl = "stat_ruler",
                stockStatus = "In Stock",
                stockCount = 30,
                brand = "Heritage Desk",
                specsJson = "Length: 15 cm / 6 inches, Material: Pure C260 Cartridge Brass, Markings: Laser Engraved"
            ),
            Product(
                title = "Celestial Desk Blotter Pad",
                description = "Premium vegan leather study desk protector in elegant deep space blue, featuring dynamic solar gold constellation maps embossed around corners. Skid-proof cork backing.",
                author = "Lunara Studio",
                category = "Stationery",
                subcategory = "Office Supplies",
                price = 1299.0,
                discountPrice = 999.0,
                rating = 4.7f,
                reviewsCount = 41,
                imageUrl = "stat_blotter",
                stockStatus = "In Stock",
                stockCount = 14,
                brand = "Lunara Studio",
                specsJson = "Size: 80cm x 40cm, Stitching: Double reinforced, Water Resistance: Liquid Spall Guard"
            )
        )
    }
}
