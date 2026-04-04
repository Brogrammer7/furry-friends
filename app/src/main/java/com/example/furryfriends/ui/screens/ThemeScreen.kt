package com.example.furryfriends.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.furryfriends.R
import com.example.furryfriends.ui.viewmodels.SettingsViewModel

enum class ThemeOption {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun ThemeScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val darkThemeOverride by viewModel.darkThemeOverride.collectAsState()

    // Determine current theme selection based on darkThemeOverride state
    val currentTheme = when (darkThemeOverride) {
        true -> ThemeOption.DARK
        false -> ThemeOption.LIGHT
        null -> ThemeOption.SYSTEM  // ✓ Handle null (system default)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Settings"
                )
            }
            Text(
                text = stringResource(R.string.change_system_theme),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Theme options
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // System theme option
            ThemeRadioOption(
                label = "System",
                isSelected = currentTheme == ThemeOption.SYSTEM,
                onClick = {
                    viewModel.setDarkThemeOverride(null)  // ✓ Pass null for system
                }
            )

            // Light theme option
            ThemeRadioOption(
                label = "Light",
                isSelected = currentTheme == ThemeOption.LIGHT,
                onClick = {
                    viewModel.setDarkThemeOverride(false)
                }
            )

            // Dark theme option
            ThemeRadioOption(
                label = "Dark",
                isSelected = currentTheme == ThemeOption.DARK,
                onClick = {
                    viewModel.setDarkThemeOverride(true)
                }
            )
        }
    }
}

@Composable
fun ThemeRadioOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
