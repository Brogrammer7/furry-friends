package com.example.furryfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.furryfriends.navigation.TabBarItem
import com.example.furryfriends.navigation.TabView
import com.example.furryfriends.ui.components.FurryFriendsAppBar
import com.example.furryfriends.ui.screens.AboutScreen
import com.example.furryfriends.ui.screens.DashboardScreen
import com.example.furryfriends.ui.screens.SearchPetsScreen
import com.example.furryfriends.ui.screens.SettingsScreen
import com.example.furryfriends.ui.theme.FurryFriendsTheme
import com.example.furryfriends.ui.viewmodels.MainActivityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = MainActivityViewModel()
            val screenTitle by viewModel.screenTitleState.collectAsState()

            val dashboardTab = TabBarItem(
                title = stringResource(R.string.dashboard_tab_title),
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard
            )
            val searchPetsTab = TabBarItem(
                title = stringResource(R.string.search_pets_tab_title),
                selectedIcon = Icons.Filled.Pets,
                unselectedIcon = Icons.Outlined.Pets
            )
            val settingsTab = TabBarItem(
                title = stringResource(R.string.settings_tab_title),
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
            val aboutTab = TabBarItem(
                title = stringResource(R.string.about_tab_title),
                selectedIcon = Icons.Filled.Info,
                unselectedIcon = Icons.Outlined.Info
            )

            val tabBarItems = listOf(dashboardTab, searchPetsTab, settingsTab, aboutTab)

            val navController = rememberNavController()

            FurryFriendsTheme {
                Scaffold(
                    topBar = { FurryFriendsAppBar(titleText = screenTitle) },
                    bottomBar = { TabView(tabBarItems = tabBarItems, navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = dashboardTab.title,
                    ) {
                        composable(dashboardTab.title) {
                            viewModel.setTitle(stringResource(R.string.dashboard_screen_title))
                            DashboardScreen(Modifier.padding(innerPadding))
                        }
                        composable(searchPetsTab.title) {
                            viewModel.setTitle(stringResource(R.string.search_pets_screen_title))
                            SearchPetsScreen(Modifier.padding(innerPadding))
                        }
                        composable(settingsTab.title) {
                            viewModel.setTitle(stringResource(R.string.settings_screen_title))
                            SettingsScreen(Modifier.padding(innerPadding))
                        }
                        composable(aboutTab.title) {
                            viewModel.setTitle(stringResource(R.string.about_screen_title))
                            AboutScreen(Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}