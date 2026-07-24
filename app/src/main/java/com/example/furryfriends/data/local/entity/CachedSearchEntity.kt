package com.example.furryfriends.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_search_results")
data class CachedSearchEntity(
    @PrimaryKey val key: String = "last_search",
    val json: String,
    val species: String? = null
)
