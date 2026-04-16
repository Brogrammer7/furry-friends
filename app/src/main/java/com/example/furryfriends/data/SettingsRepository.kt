package com.example.furryfriends.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.furryfriends.data.PreferencesKeys.DARK_THEME_KEY
import kotlinx.coroutines.flow.first

class SettingsRepository(private val context: Context) {

    // DataStore instance from the singleton extension
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore

    // Exposed Flow for dark theme setting (nullable to support system default)
    val darkThemeOverride: Flow<Boolean?> = dataStore.data.map { prefs -> prefs[DARK_THEME_KEY] }

    // Suspend function to save dark theme (accepts nullable Boolean)
    suspend fun setDarkThemeOverride(enabled: Boolean?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                if (enabled == null) {
                    prefs.remove(DARK_THEME_KEY)  // Remove the key for system default
                } else {
                    prefs[DARK_THEME_KEY] = enabled
                }
            }
        }
    }

    // Optional helper to read current value once
    suspend fun isDarkThemeOverride(): Boolean? =
        dataStore.data.map { prefs -> prefs[DARK_THEME_KEY] }.first()

    init {
        //Verify no duplicates of repo made:
        Log.d("SettingsRepo","created appHash=${context.applicationContext.hashCode()}")
    }
}
