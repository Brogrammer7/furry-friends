package com.example.furryfriends.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.furryfriends.features.settings.SettingsScreen
import com.example.furryfriends.features.settings.ThemeScreen
import com.example.furryfriends.features.settings.SettingsViewModel

@Composable
fun SettingsNavHost(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RouteSettings.Main.route,
        modifier = modifier
    ) {
        composable(RouteSettings.Main.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToTheme = {
                    navController.navigate(RouteSettings.Theme.route)
                }
            )
        }
        composable(RouteSettings.Theme.route) {
            ThemeScreen(
                viewModel = viewModel
            )
        }
    }
}