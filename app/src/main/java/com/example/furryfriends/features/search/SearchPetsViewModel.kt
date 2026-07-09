package com.example.furryfriends.features.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.data.repository.PetsRepository
import com.example.furryfriends.model.DataNode
import com.example.furryfriends.model.FilterRadius
import com.example.furryfriends.model.IncludedItem
import com.example.furryfriends.model.ResourceItem
import com.example.furryfriends.model.SearchRequest
import com.example.furryfriends.model.SearchResponse
import com.example.furryfriends.network.PetsApi
import com.example.furryfriends.network.Species
import com.example.furryfriends.util.formatPetName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val items: SearchResponse? = null,
    val error: String? = null
)

enum class SortOption {
    NONE, PET_NAME, SHELTER_NAME, CITY
}

@HiltViewModel
class SearchPetsViewModel @Inject constructor(
    private val repository: PetsRepository
): ViewModel() {

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

    private val _selectedSpecies = MutableStateFlow(Species.CATS)
    val selectedSpecies: StateFlow<Species> = _selectedSpecies.asStateFlow()

    private val _zipState = MutableStateFlow(-1)
    val zipState: StateFlow<Int> = _zipState.asStateFlow()

    private val _invalidZipProvided = MutableStateFlow(false)
    val invalidZipProvided: StateFlow<Boolean> = _invalidZipProvided.asStateFlow()

    private val _favoritePetIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePetIds: StateFlow<Set<String>> = _favoritePetIds.asStateFlow()

    private val _newResultsEvent = MutableSharedFlow<SearchResponse>(replay = 0)
    val newResultsEvent: SharedFlow<SearchResponse> = _newResultsEvent.asSharedFlow()

    data class FavoriteEvent(val petName: String, val isFavorite: Boolean)
    private val _favoriteEvent = MutableSharedFlow<FavoriteEvent>(replay = 0)
    val favoriteEvent: SharedFlow<FavoriteEvent> = _favoriteEvent.asSharedFlow()

    private val _currentSortOption = MutableStateFlow(SortOption.NONE)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                repository.favoriteIds.collectLatest { ids ->
                    _favoritePetIds.value = ids
                }
            }
            launch {
                repository.favoritePets.collectLatest { favorites ->
                    _favoriteAnimalsWithOrgs.value = favorites.map { it.animal to it.org }
                }
            }
            launch {
                repository.lastSearchResults.collectLatest { results ->
                    if (results != null && _searchUiState.value.items == null) {
                        _searchUiState.update { it.copy(items = results) }
                    }
                }
            }
        }
    }

    private val _favoriteAnimalsWithOrgs = MutableStateFlow<List<Pair<ResourceItem, IncludedItem?>>>(emptyList())
    val favoriteAnimalsWithOrgs: StateFlow<List<Pair<ResourceItem, IncludedItem?>>> = _favoriteAnimalsWithOrgs.asStateFlow()

    fun toggleFavorite(petId: String) {
        viewModelScope.launch {
            // 1. Find the animal in the current search results
            val currentResults = itemsRetrieved.value?.data ?: emptyList()
            val includedList = itemsRetrieved.value?.included
            val animalInResults = currentResults.find { it.id == petId }

            if (animalInResults != null) {
                val org = getOrganizationForAnimal(animalInResults, includedList)
                val isCurrentlyFavorite = favoritePetIds.value.contains(petId)
                repository.toggleFavorite(animalInResults, org)
                
                val formattedName = formatPetName(animalInResults.attributes.name)
                _favoriteEvent.emit(FavoriteEvent(formattedName, !isCurrentlyFavorite))
            } else {
                // 2. If not in current results, it must be in the favorites list already (to be removed)
                val existingFavorite = _favoriteAnimalsWithOrgs.value.find { it.first.id == petId }
                if (existingFavorite != null) {
                    repository.toggleFavorite(existingFavorite.first, existingFavorite.second)
                    
                    val formattedName = formatPetName(existingFavorite.first.attributes.name)
                    _favoriteEvent.emit(FavoriteEvent(formattedName, false))
                }
            }
        }
    }

    fun processZipInput(raw: String) {
        val filtered = raw.filter { it.isDigit() }.take(5)
        if (filtered.isEmpty()) {
            _zipState.value = -1
        } else {
            val asInt = filtered.toInt()
            _zipState.value = asInt
        }
    }

    fun checkValidZip(zip: Int): Boolean {
        if (zip in 10000..99999) return true else return false
    }

    fun clearZip() {
        _invalidZipProvided.update { false }
        _zipState.update { -1 }
    }

    fun searchPetData(petType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchUiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            // Artificial delay for search query to show off SpinningLoader
            delay(1200)
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
                            miles = setSearchRange(),
                            postalCode = zipState.value
                        )
                    )
                )

                val response = PetsApi.retrofitService.searchPets(species = petType, body = requestBody)

                if (response.isSuccessful) {
                    val body = response.body()
                    _searchUiState.update {
                        it.copy(
                            items = body,
                            isLoading = false,
                            error = null
                        )
                    }
                    if (body != null) {
                        repository.saveSearchResults(body)
                        _newResultsEvent.emit(body)
                    }
                } else {
                    val finalError = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                .decodeFromString<SearchResponse>(errorBody)
                                .errors
                                ?.firstOrNull()
                                ?.detail
                        } else null
                    } catch (ex: Exception) {
                        null
                    } ?: response.message() ?: "Unknown API error"

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

    fun clearSearchData() {
        _invalidZipProvided.update { false }
        _searchUiState.update {
            it.copy(
                items = null,
                isLoading = false,
                error = null)
        }
        viewModelScope.launch {
            repository.clearSearchResults()
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
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

    fun getAnimalsWithOrgs(
        searchList: List<ResourceItem>?,
        includedList: List<IncludedItem>?
    ): List<Pair<ResourceItem, IncludedItem?>> {
        val list = searchList?.map { animal ->
            animal to getOrganizationForAnimal(animal, includedList)
        } ?: emptyList()

        return when (_currentSortOption.value) {
            SortOption.NONE -> list
            SortOption.PET_NAME -> list.sortedBy { it.first.attributes.name?.lowercase() ?: "" }
            SortOption.SHELTER_NAME -> list.sortedBy { it.second?.attributes?.name?.lowercase() ?: "" }
            SortOption.CITY -> list.sortedBy { it.second?.attributes?.city?.lowercase() ?: "" }
        }
    }

    fun updateSortOption(option: SortOption) {
        _currentSortOption.value = option
    }

    fun updateSelectedSpecies(species: Species) {
        _selectedSpecies.value = species
    }

    fun setSearchRange(): Int {
        return when (selectedSpecies.value) {
            Species.CATS -> 10
            Species.DOGS -> 10
            Species.RABBITS -> 100
            Species.BIRDS -> 500
            Species.HORSES -> 1000
        }
    }

}