package com.example.furryfriends.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.model.Pets
import com.example.furryfriends.network.PetsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class PetsUiState(
    val items: Pets? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FindPetsViewModel : ViewModel() {

    private val _petsUiState = MutableStateFlow(PetsUiState())
    val petsUiState: StateFlow<PetsUiState> = _petsUiState.asStateFlow()

//    init {
//        getPetData()
//    }

    fun getPetData() {
        viewModelScope.launch {
            // show loading
            _petsUiState.value = _petsUiState.value.copy(isLoading = true, error = null)

            try {
                val petsApiResult: Pets = PetsApi.retrofitService.getAvailablePets()
                Log.d("FindPetsViewModel", "API data is = $petsApiResult")

                // update state with result
                _petsUiState.value = _petsUiState.value.copy(
                    items = petsApiResult,
                    isLoading = false,
                    error = null
                )
            } catch (e: IOException) {
                Log.e("FindPetsViewModel", "IO error fetching pets", e)
                _petsUiState.value = _petsUiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message ?: "unknown"}"
                )
            } catch (e: Exception) {
                Log.e("FindPetsViewModel", "Unexpected error fetching pets", e)
                _petsUiState.value = _petsUiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message ?: "unknown"}"
                )
            }
        }
    }

}