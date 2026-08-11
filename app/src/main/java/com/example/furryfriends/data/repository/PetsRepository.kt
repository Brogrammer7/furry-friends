package com.example.furryfriends.data.repository

import com.example.furryfriends.data.local.dao.CachedSearchDao
import com.example.furryfriends.data.local.dao.FavoritePetDao
import com.example.furryfriends.data.local.entity.CachedSearchEntity
import com.example.furryfriends.data.local.entity.FavoritePetEntity
import com.example.furryfriends.data.remote.dto.IncludedItem
import com.example.furryfriends.data.remote.dto.ResourceItem
import com.example.furryfriends.data.remote.dto.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class FavoritePet(
    val animal: ResourceItem,
    val org: IncludedItem?
)

data class CachedSearch(
    val response: SearchResponse,
    val species: String
)

@Singleton
class PetsRepository @Inject constructor(
    private val favoritePetDao: FavoritePetDao,
    private val cachedSearchDao: CachedSearchDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val lastSearchWithSpecies: Flow<CachedSearch?> = cachedSearchDao.getLastSearch()
        .map { entity ->
            val response = entity?.json?.let { jsonString ->
                try {
                    json.decodeFromString<SearchResponse>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
            if (response != null && entity.species != null) {
                CachedSearch(response, entity.species)
            } else {
                null
            }
        }.distinctUntilChanged()

    val favoriteIds: Flow<Set<String>> = favoritePetDao.getAllFavoriteIds()
        .map { it.toSet() }
        .distinctUntilChanged()

    val favoritePets: Flow<List<FavoritePet>> = favoritePetDao.getAllFavorites()
        .map { entities ->
            entities.mapNotNull { entity ->
                try {
                    json.decodeFromString<FavoritePet>(entity.petJson)
                } catch (e: Exception) {
                    null
                }
            }
        }.distinctUntilChanged()

    suspend fun saveSearchResults(response: SearchResponse, species: String) {
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(response)
            cachedSearchDao.insertLastSearch(CachedSearchEntity(json = jsonString, species = species))
        }
    }

    suspend fun clearSearchResults() {
        withContext(Dispatchers.IO) {
            cachedSearchDao.clear()
        }
    }

    suspend fun clearAllFavorites() {
        withContext(Dispatchers.IO) {
            favoritePetDao.deleteAll()
        }
    }

    suspend fun toggleFavorite(animal: ResourceItem, org: IncludedItem?) {
        withContext(Dispatchers.IO) {
            val petId = animal.id
            if (favoritePetDao.isFavorite(petId)) {
                favoritePetDao.deleteFavorite(petId)
            } else {
                val petData = FavoritePet(animal, org)
                val jsonString = json.encodeToString(petData)
                favoritePetDao.insertFavorite(FavoritePetEntity(petId, jsonString))
            }
        }
    }
}
