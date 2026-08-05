package com.example.furryfriends.features.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.data.repository.DashboardRepository
import com.example.furryfriends.data.repository.PetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val petsRepository: PetsRepository
) : ViewModel() {
    private val TAG = "DashboardViewModel"

    private val _dashboardImage = MutableStateFlow<String?>(null)
    val dashboardImage: StateFlow<String?> = _dashboardImage.asStateFlow()

    val savedPetsCount: StateFlow<Int> = petsRepository.favoriteIds
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

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
