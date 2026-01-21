package com.example.furryfriends

import android.app.Application
import com.example.furryfriends.data.SettingsRepository

class App : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }
}