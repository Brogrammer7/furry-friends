package com.example.furryfriends.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

    private val _screenTitleState = MutableStateFlow("")
    val screenTitleState: StateFlow<String> = _screenTitleState.asStateFlow()

    fun setTitle(newTitle: String) {
        _screenTitleState.update { newTitle }
    }
}