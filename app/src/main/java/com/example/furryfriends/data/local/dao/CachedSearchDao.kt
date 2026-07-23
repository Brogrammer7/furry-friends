package com.example.furryfriends.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.furryfriends.data.local.entity.CachedSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedSearchDao {
    @Query("SELECT * FROM cached_search_results WHERE `key` = 'last_search'")
    fun getLastSearch(): Flow<CachedSearchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastSearch(results: CachedSearchEntity)

    @Query("DELETE FROM cached_search_results WHERE `key` = 'last_search'")
    suspend fun clear()
}
