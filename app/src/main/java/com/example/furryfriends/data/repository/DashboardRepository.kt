package com.example.furryfriends.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.furryfriends.data.local.PreferencesKeys.DASHBOARD_IMAGE_KEY
import com.example.furryfriends.data.local.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.applicationContext.dataStore

    val dashboardImage: Flow<String?> = dataStore.data.map { prefs -> prefs[DASHBOARD_IMAGE_KEY] }

    suspend fun setDashboardImage(uri: String?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                if (uri == null) {
                    prefs.remove(DASHBOARD_IMAGE_KEY)
                } else {
                    prefs[DASHBOARD_IMAGE_KEY] = uri
                }
            }
        }
    }

    suspend fun getDashboardImage(): String? = dataStore.data.map { prefs -> prefs[DASHBOARD_IMAGE_KEY] }.first()
}
