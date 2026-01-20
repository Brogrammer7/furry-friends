package com.example.furryfriends.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_NAME = "settings"

val Context.dataStore by preferencesDataStore(name = SETTINGS_NAME)