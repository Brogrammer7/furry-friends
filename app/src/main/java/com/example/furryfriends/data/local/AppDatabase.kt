package com.example.furryfriends.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.furryfriends.data.local.dao.CachedSearchDao
import com.example.furryfriends.data.local.dao.FavoritePetDao
import com.example.furryfriends.data.local.entity.CachedSearchEntity
import com.example.furryfriends.data.local.entity.FavoritePetEntity

@Database(
    entities = [
        FavoritePetEntity::class,
        CachedSearchEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritePetDao(): FavoritePetDao
    abstract fun cachedSearchDao(): CachedSearchDao
}
