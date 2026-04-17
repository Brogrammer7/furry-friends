package com.example.furryfriends.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    val ZIP_KEY = stringPreferencesKey("zip")
}