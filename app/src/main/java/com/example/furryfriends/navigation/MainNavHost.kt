package com.example.furryfriends.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.furryfriends.R
import com.example.furryfriends.ui.screens.AboutScreen
import com.example.furryfriends.ui.screens.DashboardScreen
import com.example.furryfriends.ui.screens.SavedPetsScreen
import com.example.furryfriends.ui.screens.SearchPetsScreen
import com.example.furryfriends.ui.viewmodels.MainActivityViewModel
import com.example.furryfriends.ui.viewmodels.SearchPetsViewModel
import com.example.furryfriends.ui.viewmodels.SettingsViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainActivityViewModel,
    settingsViewModel: SettingsViewModel,
    searchPetsViewModel: SearchPetsViewModel,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = RouteMain.Dashboard.route
    ) {
        composable(RouteMain.Dashboard.route) {
            mainViewModel.setTitle(stringResource(R.string.dashboard_screen_title))
            DashboardScreen(
                modifier = Modifier.padding(innerPadding),
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
            SettingsNavHost(
                modifier = Modifier.padding(innerPadding),
                viewModel = settingsViewModel
            )
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
    }
}