package com.example.furryfriends.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    val ZIP_KEY = stringPreferencesKey("zip")
    val DASHBOARD_IMAGE_KEY = stringPreferencesKey("dashboard_image")
}
