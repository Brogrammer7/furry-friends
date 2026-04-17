package com.example.furryfriends.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_NAME = "settings"

/* This file creates a single, shared DataStore instance across the entire app using Kotlin's extension property pattern. It ensures only one DataStore object exists. */
val Context.dataStore by preferencesDataStore(name = SETTINGS_NAME)