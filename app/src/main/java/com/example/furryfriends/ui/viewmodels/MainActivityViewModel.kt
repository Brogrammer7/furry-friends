package com.example.furryfriends.ui.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivityViewModel() {

    private var _screenTitleState = MutableStateFlow("")
    val screenTitleState: StateFlow<String> = _screenTitleState.asStateFlow()

    fun setTitle(newTitle: String) {
        _screenTitleState.update { newTitle }
    }
}