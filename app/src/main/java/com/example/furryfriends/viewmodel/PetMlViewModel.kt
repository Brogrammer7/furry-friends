package com.example.furryfriends.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.R
import com.example.furryfriends.ml.PetAnalyzer
import com.example.furryfriends.domain.model.Species
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetMlViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val petAnalyzer: PetAnalyzer
) : ViewModel() {
    private var dismissJob: Job? = null
    
    private val _displayText = MutableStateFlow("")
    val displayText: StateFlow<String> = _displayText.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun analyzePetImage(bitmap: Bitmap, expectedSpecies: Species?) {
        dismissJob?.cancel()
        viewModelScope.launch {
            _isAnalyzing.value = true
            petAnalyzer.analyzeImage(
                bitmap = bitmap,
                onSuccess = { result ->
                    _isAnalyzing.value = false
                    
                    val fallbackLabel = expectedSpecies?.mlLabel?.lowercase() ?: "pet"

                    _displayText.value = when {
                        // 1. Breed or Multiple Labels found
                        result.size > 1 || (result.isNotEmpty() && !Species.allMlLabels.contains(result[0])) -> {
                            result.joinToString(", ")
                        }
                        // 2. Only Generic Species found (e.g. "Dog")
                        result.size == 1 && Species.allMlLabels.contains(result[0]) -> {
                            applicationContext.getString(
                                R.string.ml_no_breed_found, 
                                result[0].lowercase()
                            )
                        }
                        // 3. No labels found: use smart guess fallback
                        else -> {
                            applicationContext.getString(R.string.ml_no_breed_found, fallbackLabel)
                        }
                    }
                    
                    // Auto-dismiss after 5 seconds
                    dismissJob = viewModelScope.launch {
                        delay(5000)
                        clearLabels()
                    }
                },
                onFailure = {
                    _isAnalyzing.value = false
                    val fallbackLabel = expectedSpecies?.mlLabel?.lowercase() ?: "pet"
                    _displayText.value = applicationContext.getString(R.string.ml_no_breed_found, fallbackLabel)
                }
            )
        }
    }

    fun clearLabels() {
        dismissJob?.cancel()
        _displayText.value = ""
    }
}
