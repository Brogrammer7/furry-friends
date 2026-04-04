package com.example.furryfriends.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.furryfriends.ui.screens.SettingsScreen
import com.example.furryfriends.ui.screens.ThemeScreen
import com.example.furryfriends.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsNavGraph(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val settingsNavController = rememberNavController()

    NavHost(
        navController = settingsNavController,
        startDestination = "settings_main",
        modifier = modifier
    ) {
        composable("settings_main") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToTheme = {
                    settingsNavController.navigate("theme")
                }
            )
        }
        composable("theme") {
            ThemeScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    settingsNavController.popBackStack()
                }
            )
        }
    }
}
