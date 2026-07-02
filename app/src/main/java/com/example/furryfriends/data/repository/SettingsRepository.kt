package com.example.furryfriends.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.furryfriends.data.local.dataStore
import com.example.furryfriends.data.local.PreferencesKeys.DARK_THEME_KEY
import com.example.furryfriends.data.local.PreferencesKeys.ZIP_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    // DataStore instance from the singleton extension
    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore

    // Exposed Flow for dark theme setting (nullable to support system default)
    val darkThemeOverride: Flow<Boolean?> = dataStore.data.map { prefs -> prefs[DARK_THEME_KEY] }

    val zip: Flow<String?> = dataStore.data.map { prefs -> prefs[ZIP_KEY] }

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

    suspend fun setZip(zip: String?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                if (zip == null) {
                    prefs.remove(ZIP_KEY)
                } else {
                    prefs[ZIP_KEY] = zip
                }
            }
        }
    }

    suspend fun getZip(): String? = dataStore.data.map { prefs -> prefs[ZIP_KEY] }.first()

    init {
        //Verify no duplicates of repo made:
        Log.d("SettingsRepo","created appHash=${context.applicationContext.hashCode()}")
    }
}
