package com.example.furryfriends.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferencesKeys {
    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    val ZIP_KEY = stringPreferencesKey("zip")
    val LAST_SEARCH_RESULTS_KEY = stringPreferencesKey("last_search_results")
    val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_ids")
    val FAVORITE_PETS_DATA_KEY = stringPreferencesKey("favorite_pets_data")
}
