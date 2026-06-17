package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Product::class,
        CartItem::class,
        WishlistItem::class,
        Order::class,
        UserAddress::class,
        NotificationItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun orderDao(): OrderDao
    abstract fun addressDao(): AddressDao
    abstract fun notificationDao(): NotificationDao
}
