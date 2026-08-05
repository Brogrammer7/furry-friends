package com.example.furryfriends

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.furryfriends.features.auth.LoginScreen
import com.example.furryfriends.ui.theme.FurryFriendsTheme
import com.example.furryfriends.features.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isLoggedIn by settingsViewModel.isLoggedIn.collectAsState()
            
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            val darkThemeOverride by settingsViewModel.darkThemeOverride.collectAsState()
            val darkThemeLogic = darkThemeOverride ?: isSystemInDarkTheme()

            val navController = rememberNavController()

            FurryFriendsTheme(darkTheme = darkThemeLogic) {
                Surface(
                    tonalElevation = 5.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }


        }
    }

}