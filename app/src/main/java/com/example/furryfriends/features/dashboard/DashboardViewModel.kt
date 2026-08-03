package com.example.furryfriends.features.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {
    private val TAG = "DashboardViewModel"

    private val _dashboardImage = MutableStateFlow<String?>(null)
    val dashboardImage: StateFlow<String?> = _dashboardImage.asStateFlow()

    init {
        viewModelScope.launch {
            // Load initial value
            _dashboardImage.value = repository.getDashboardImage()
            // Sync with repository updates
            launch {
                repository.dashboardImage.collectLatest {
                    _dashboardImage.value = it
                }
            }
        }
    }

    fun setDashboardImage(uri: String?) {
        _dashboardImage.value = uri
        viewModelScope.launch {
            try {
                repository.setDashboardImage(uri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist dashboard image", e)
            }
        }
    }
}
