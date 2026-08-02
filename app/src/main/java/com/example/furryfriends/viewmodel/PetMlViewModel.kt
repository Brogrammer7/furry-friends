package com.example.furryfriends.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.ml.PetAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class PetMlViewModel @Inject constructor(
    private val petAnalyzer: PetAnalyzer
) : ViewModel() {
    private var dismissJob: Job? = null
    
    private val _labels = MutableStateFlow<List<String>>(emptyList())
    val labels = _labels.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    fun analyzePetImage(bitmap: Bitmap) {
        dismissJob?.cancel()
        viewModelScope.launch {
            _isAnalyzing.value = true
            petAnalyzer.analyzeImage(
                bitmap = bitmap,
                onSuccess = { result ->
                    _labels.value = result
                    _isAnalyzing.value = false
                    
                    // Auto-dismiss labels after 5 seconds
                    dismissJob = viewModelScope.launch {
                        delay(5000)
                        _labels.value = emptyList()
                    }
                },
                onFailure = {
                    _isAnalyzing.value = false
                    // Handle error
                }
            )
        }
    }

    fun clearLabels() {
        dismissJob?.cancel()
        _labels.value = emptyList()
    }
}
