package com.example.furryfriends.navigation

sealed class RouteMain(val route: String) {
    data object Dashboard : RouteMain("dashboard")
    data object Search : RouteMain("search")
    data object Settings : RouteMain("settings")
    data object About : RouteMain("about")
}
