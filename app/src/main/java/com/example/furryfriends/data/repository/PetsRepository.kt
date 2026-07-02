package com.example.furryfriends.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.furryfriends.data.local.dataStore
import com.example.furryfriends.data.local.PreferencesKeys.FAVORITE_IDS_KEY
import com.example.furryfriends.data.local.PreferencesKeys.FAVORITE_PETS_DATA_KEY
import com.example.furryfriends.data.local.PreferencesKeys.LAST_SEARCH_RESULTS_KEY
import com.example.furryfriends.model.IncludedItem
import com.example.furryfriends.model.ResourceItem
import com.example.furryfriends.model.SearchResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class FavoritePet(
    val animal: ResourceItem,
    val org: IncludedItem?
)

@Singleton
class PetsRepository @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore
    private val gson = Gson()

    val lastSearchResults: Flow<SearchResponse?> = dataStore.data.map { prefs ->
        prefs[LAST_SEARCH_RESULTS_KEY]?.let { json ->
            try {
                gson.fromJson(json, SearchResponse::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    val favoriteIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FAVORITE_IDS_KEY] ?: emptySet()
    }

    val favoritePets: Flow<List<FavoritePet>> = dataStore.data.map { prefs ->
        prefs[FAVORITE_PETS_DATA_KEY]?.let { json ->
            try {
                val type = object : TypeToken<List<FavoritePet>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun saveSearchResults(response: SearchResponse) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(response)
            dataStore.edit { prefs ->
                prefs[LAST_SEARCH_RESULTS_KEY] = json
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
                val type = object : TypeToken<MutableList<FavoritePet>>() {}.type
                val favoritePetsList: MutableList<FavoritePet> = if (favoritePetsJson != null) {
                    try {
                        gson.fromJson(favoritePetsJson, type)
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
                
                prefs[FAVORITE_PETS_DATA_KEY] = gson.toJson(favoritePetsList)
            }
        }
    }
}
