package com.example.furryfriends.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.furryfriends.data.local.PreferencesKeys.FAVORITE_IDS_KEY
import com.example.furryfriends.data.local.PreferencesKeys.FAVORITE_PETS_DATA_KEY
import com.example.furryfriends.data.local.PreferencesKeys.LAST_SEARCH_RESULTS_KEY
import com.example.furryfriends.data.local.dataStore
import com.example.furryfriends.model.IncludedItem
import com.example.furryfriends.model.ResourceItem
import com.example.furryfriends.model.SearchResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

@Singleton
class PetsRepository @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val lastSearchResults: Flow<SearchResponse?> = dataStore.data.map { prefs ->
        prefs[LAST_SEARCH_RESULTS_KEY]?.let { jsonString ->
            try {
                json.decodeFromString<SearchResponse>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    val favoriteIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FAVORITE_IDS_KEY] ?: emptySet()
    }

    val favoritePets: Flow<List<FavoritePet>> = dataStore.data.map { prefs ->
        prefs[FAVORITE_PETS_DATA_KEY]?.let { jsonString ->
            try {
                json.decodeFromString<List<FavoritePet>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun saveSearchResults(response: SearchResponse) {
        withContext(Dispatchers.IO) {
            val jsonString = json.encodeToString(response)
            dataStore.edit { prefs ->
                prefs[LAST_SEARCH_RESULTS_KEY] = jsonString
            }
        }
    }

    suspend fun clearSearchResults() {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs.remove(LAST_SEARCH_RESULTS_KEY)
            }
        }
    }

    suspend fun clearAllFavorites() {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs.remove(FAVORITE_IDS_KEY)
                prefs.remove(FAVORITE_PETS_DATA_KEY)
            }
        }
    }

    suspend fun toggleFavorite(animal: ResourceItem, org: IncludedItem?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                val currentIds = prefs[FAVORITE_IDS_KEY] ?: emptySet()
                val petId = animal.id
                
                val favoritePetsJson = prefs[FAVORITE_PETS_DATA_KEY]
                val favoritePetsList: MutableList<FavoritePet> = if (favoritePetsJson != null) {
                    try {
                        json.decodeFromString<List<FavoritePet>>(favoritePetsJson).toMutableList()
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }

                if (currentIds.contains(petId)) {
                    prefs[FAVORITE_IDS_KEY] = currentIds - petId
                    favoritePetsList.removeAll { it.animal.id == petId }
                } else {
                    prefs[FAVORITE_IDS_KEY] = currentIds + petId
                    favoritePetsList.add(FavoritePet(animal, org))
                }
                
                prefs[FAVORITE_PETS_DATA_KEY] = json.encodeToString(favoritePetsList.toList())
            }
        }
    }
}
