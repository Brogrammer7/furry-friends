package com.example.furryfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.furryfriends.navigation.MainNavHost
import com.example.furryfriends.navigation.RouteMain
import com.example.furryfriends.navigation.TabBarItem
import com.example.furryfriends.navigation.TabView
import com.example.furryfriends.ui.components.FurryFriendsAppBar
import com.example.furryfriends.ui.theme.FurryFriendsTheme
import com.example.furryfriends.MainActivityViewModel
import com.example.furryfriends.features.search.SearchPetsViewModel
import com.example.furryfriends.features.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainActivityViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val searchPetsViewModel: SearchPetsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val screenTitle by mainViewModel.screenTitleState.collectAsState()
            val darkThemeOverride by settingsViewModel.darkThemeOverride.collectAsState()
            val darkThemeLogic = darkThemeOverride ?: isSystemInDarkTheme()

            val favoritePetIds by searchPetsViewModel.favoritePetIds.collectAsState()
            val favoriteCount = if (favoritePetIds.isEmpty()) null else favoritePetIds.size

            val dashboardTab = TabBarItem(
                title = stringResource(R.string.dashboard_tab_title),
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                iconTicker = favoriteCount
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val canNavigateBack = currentRoute != null && 
                                  currentRoute != RouteMain.Dashboard.route && 
                                  currentRoute != RouteMain.Search.route && 
                                  currentRoute != RouteMain.Settings.route && 
                                  currentRoute != RouteMain.About.route

            FurryFriendsTheme(darkTheme = darkThemeLogic) {
                Surface(
                    tonalElevation = 5.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(
                        topBar = {
                            FurryFriendsAppBar(
                                titleText = screenTitle,
                                navigationIcon = {
                                    if (canNavigateBack) {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            TabView(
                                tabBarItems = tabBarItems,
                                navController = navController
                            )
                        }
                    ) { innerPadding ->
                        MainNavHost(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            settingsViewModel = settingsViewModel,
                            searchPetsViewModel = searchPetsViewModel,
                            innerPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}