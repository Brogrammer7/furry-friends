package com.example.furryfriends

import android.app.Application
import com.example.furryfriends.data.PetsRepository
import com.example.furryfriends.data.SettingsRepository

class App : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    val petsRepository: PetsRepository by lazy {
        PetsRepository(applicationContext)
    }
}