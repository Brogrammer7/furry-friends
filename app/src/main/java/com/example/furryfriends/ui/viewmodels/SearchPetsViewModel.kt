package com.example.furryfriends.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.model.DataNode
import com.example.furryfriends.model.FilterRadius
import com.example.furryfriends.model.IncludedItem
import com.example.furryfriends.model.ResourceItem
import com.example.furryfriends.model.SearchRequest
import com.example.furryfriends.model.SearchResponse
import com.example.furryfriends.network.PetsApi
import com.example.furryfriends.network.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class SearchUiState(
    val isLoading: Boolean = false,
    val items: SearchResponse? = null,
    val error: String? = null
)

class SearchPetsViewModel: ViewModel() {

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    val isLoadingOn: StateFlow<Boolean> = searchUiState
        .map { it.isLoading }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val itemsRetrieved: StateFlow<SearchResponse?> = searchUiState
        .map { it.items }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val errorReceived: StateFlow<String?> = searchUiState
        .map { it.error }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _zipState = MutableStateFlow(-1)
    val zipState: StateFlow<Int> = _zipState.asStateFlow()

    private val _zipError = MutableStateFlow(false)
    val zipError: StateFlow<Boolean> = _zipError.asStateFlow()

    private val _invalidZipProvided = MutableStateFlow(false)
    val invalidZipProvided: StateFlow<Boolean> = _invalidZipProvided.asStateFlow()

    private val _selectedSpecies = MutableStateFlow(Species.CATS)
    val selectedSpecies: StateFlow<Species> = _selectedSpecies.asStateFlow()

    fun updateZipInput(raw: String) {
        val filtered = raw.filter { it.isDigit() }.take(5)
        if (filtered.isEmpty()) {
            _zipState.value = -1
            _zipError.value = false
        } else {
            val asInt = filtered.toInt()
            _zipState.value = asInt
            _zipError.value = filtered.length != 5
        }
    }

    fun searchPetData(petType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchUiState.update {
                it.copy(
                    isLoading = true,
                    error = null) }
            try {
                val requestBody = SearchRequest(
                    data = DataNode(
                        //TODO server doesn't seem to acknowledge filters despite what documentation says
//                    filters = listOf(
//                        Filter("statuses.name", "equals", "Available"),
//                        Filter("species.singular", "equals", "Cat"),
//                        Filter("species.singular", "equals", "Dog"),
//                    ),
//                    filterProcessing = "1 AND (2 OR 3)",
                        filterRadius = FilterRadius(
                            miles = 10,
                            postalCode = zipState.value
                        )
                    )
                )

                val response = PetsApi.retrofitService.searchPets(species = petType, body = requestBody)

                if (response.isSuccessful) {
                    _searchUiState.update {
                        it.copy(
                            items = response.body(),
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val apiErrorDetail = try {
                        // Gson will map to your SearchResponse and ApiError types
                        com.google.gson.Gson()
                            .fromJson(errorBody, SearchResponse::class.java)
                            ?.errors
                            ?.firstOrNull()
                            ?.detail
                    } catch (ex: Exception) {
                        null
                    }

                    val finalError = apiErrorDetail ?: response.message() ?: "Unknown API error"

                    _searchUiState.update {
                        it.copy(
                            isLoading = false,
                            error = finalError
                        )
                    }

                    _invalidZipProvided.update {
                        finalError.contains("not a recognized postalcode", ignoreCase = true)
                    }
                }

            } catch (e: IOException) {
                _searchUiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown IO error")
                }
                Log.e("check2", "IOException", e)
            } catch (e: Exception) {
                _searchUiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
                Log.e("check2", "Exception", e)
            }
        }
    }

    fun clearZip() {
        _invalidZipProvided.update { false }
        _zipState.update { -1 }
        _zipError.update { false }
    }

    fun clearSearchData() {
        _invalidZipProvided.update { false }
        _searchUiState.update {
            it.copy(
                items = null,
                isLoading = false,
                error = null)
        }
    }

    fun getOrganizationForAnimal(
        animal: ResourceItem,
        includedList: List<IncludedItem>?
    ): IncludedItem? {
        // get first org relationship id for this animal (if any)
        val orgRelId = animal.relationships.orgs?.data?.firstOrNull()?.id
        // find included org by id and type "orgs"
        return includedList?.find { it.id == orgRelId && it.type == "orgs" }
    }

}