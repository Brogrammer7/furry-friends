package com.example.furryfriends.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.furryfriends.R
import com.example.furryfriends.features.about.AboutScreen
import com.example.furryfriends.features.dashboard.DashboardScreen
import com.example.furryfriends.features.savedpets.SavedPetsScreen
import com.example.furryfriends.features.search.SearchPetsScreen
import com.example.furryfriends.features.search.SearchPetsViewModel
import com.example.furryfriends.features.settings.SettingsScreen
import com.example.furryfriends.features.settings.SettingsViewModel
import com.example.furryfriends.features.settings.ThemeScreen
import com.example.furryfriends.MainActivityViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainActivityViewModel,
    settingsViewModel: SettingsViewModel,
    searchPetsViewModel: SearchPetsViewModel,
    innerPadding: PaddingValues
) {
    val favoritePetIds by searchPetsViewModel.favoritePetIds.collectAsState()

    NavHost(
        navController = navController,
        startDestination = RouteMain.Dashboard.route
    ) {
        composable(RouteMain.Dashboard.route) {
            mainViewModel.setTitle(stringResource(R.string.dashboard_screen_title))
            DashboardScreen(
                modifier = Modifier.padding(innerPadding),
                savedPetsCount = favoritePetIds.size,
                onViewSavedPetsClick = { navController.navigate(RouteMain.SavedPets.route) }
            )
        }
        composable(RouteMain.Search.route) {
            mainViewModel.setTitle(stringResource(R.string.search_pets_screen_title))
            SearchPetsScreen(
                modifier = Modifier.padding(innerPadding),
                settingsViewModel = settingsViewModel,
                viewModel = searchPetsViewModel
            )
        }
        composable(RouteMain.Settings.route) {
            mainViewModel.setTitle(stringResource(R.string.settings_screen_title))
            SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = settingsViewModel
            ) {
                navController.navigate(RouteMain.Theme.route)
            }
        }
        composable(RouteMain.About.route) {
            mainViewModel.setTitle(stringResource(R.string.about_screen_title))
            AboutScreen(modifier = Modifier.padding(innerPadding))
        }
        composable(RouteMain.SavedPets.route) {
            mainViewModel.setTitle(stringResource(R.string.saved_pets_screen_title))
            SavedPetsScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = searchPetsViewModel
            )
        }
        composable(RouteMain.Theme.route) {
            mainViewModel.setTitle(stringResource(R.string.change_system_theme_screen_title))
            ThemeScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = settingsViewModel
            )
        }
    }
}