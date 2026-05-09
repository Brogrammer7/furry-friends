package com.example.furryfriends.navigation

sealed class RouteSettings(val route: String) {
    data object Main : RouteSettings("settings_main")
    data object Theme : RouteSettings("theme")
}