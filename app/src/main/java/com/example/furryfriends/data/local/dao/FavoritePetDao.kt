package com.example.furryfriends.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.furryfriends.data.local.entity.FavoritePetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePetDao {
    @Query("SELECT * FROM favorite_pets")
    fun getAllFavorites(): Flow<List<FavoritePetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(pet: FavoritePetEntity)

    @Query("DELETE FROM favorite_pets WHERE id = :petId")
    suspend fun deleteFavorite(petId: String)

    @Query("DELETE FROM favorite_pets")
    suspend fun deleteAll()

    @Query("SELECT id FROM favorite_pets")
    fun getAllFavoriteIds(): Flow<List<String>>
}
