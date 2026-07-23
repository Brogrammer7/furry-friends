package com.example.furryfriends.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pets")
data class FavoritePetEntity(
    @PrimaryKey val id: String,
    val petJson: String // Serialized ResourceItem + IncludedItem
)
