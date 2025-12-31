package com.example.furryfriends.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.model.Pets
import com.example.furryfriends.network.PetsApi
import kotlinx.coroutines.delay
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

    init {
        _petsUiState.value = _petsUiState.value.copy(
            items = null,
            isLoading = false,
            error = null
        )
    }

    fun getPetData() {
        val maxRetries = 5
        val initialDelayMs = 500L

        viewModelScope.launch {
            _petsUiState.value = _petsUiState.value.copy(isLoading = true, error = null)

            var attempt = 0
            var lastError: Throwable? = null

            while (attempt <= maxRetries) {
                try {
                    val petsApiResult: Pets = PetsApi.retrofitService.getAvailablePets()

                    _petsUiState.value = _petsUiState.value.copy(
                        items = filterAvailablePets(petsApiResult),
                        isLoading = false,
                        error = null
                    )
                    if (attempt == 0) Log.i("check1", "*** Success connecting on first try! ***") else Log.i("check1", "*** Success after $attempt retry attempt(s) ***")
                    return@launch
                } catch (e: IOException) {
                    // network error -> retry
                    attempt++
                    lastError = e
                    Log.w("check1", "Network attempt $attempt failed", e)
                    if (attempt > maxRetries) break
                    val backoff = initialDelayMs * (1 shl (attempt - 1))
                    delay(backoff)
                } catch (e: Exception) {
                    // non-network error -> stop retrying
                    Log.e("FindPetsViewModel", "Unexpected error fetching pets", e)
                    _petsUiState.value = _petsUiState.value.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message ?: "unknown"}"
                    )
                    return@launch
                }
            }

            // If we get here, retries exhausted or lastError set
            Log.e("check1", "Failed to fetch pets after $attempt attempts", lastError)
            _petsUiState.value = _petsUiState.value.copy(
                isLoading = false,
                error = "Network error after $attempt attempts: ${lastError?.message ?: "unknown"}"
            )
        }
    }

    fun filterAvailablePets(petData: Pets): Pets {
        val filteredData = petData.data.filter { item ->
            item?.attributes?.name?.contains("adopted", ignoreCase = true) == false
        }

        return petData.copy(data = filteredData)
    }

    fun clearPetData() {
        _petsUiState.value = _petsUiState.value.copy(
            items = null,
            isLoading = false,
            error = null
        )
    }

}