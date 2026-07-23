package com.example.furryfriends.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class ThemeOption {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun ThemeScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val darkThemeOverride by viewModel.darkThemeOverride.collectAsState()

    ThemeContent(
        modifier = modifier,
        darkThemeOverride = darkThemeOverride,
        onThemeOptionSelected = { viewModel.setDarkThemeOverride(it) }
    )
}

@Composable
fun ThemeContent(
    modifier: Modifier = Modifier,
    darkThemeOverride: Boolean?,
    onThemeOptionSelected: (Boolean?) -> Unit
) {
    // Determine current theme selection based on darkThemeOverride state
    val currentTheme = when (darkThemeOverride) {
        true -> ThemeOption.DARK
        false -> ThemeOption.LIGHT
        null -> ThemeOption.SYSTEM  // ✓ Handle null (system default)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Theme options
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // System theme option
            ThemeRadioOption(
                label = "System",
                isSelected = currentTheme == ThemeOption.SYSTEM,
                onClick = {
                    onThemeOptionSelected(null)  // ✓ Pass null for system
                }
            )

            // Light theme option
            ThemeRadioOption(
                label = "Light",
                isSelected = currentTheme == ThemeOption.LIGHT,
                onClick = {
                    onThemeOptionSelected(false)
                }
            )

            // Dark theme option
            ThemeRadioOption(
                label = "Dark",
                isSelected = currentTheme == ThemeOption.DARK,
                onClick = {
                    onThemeOptionSelected(true)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeScreenPreview() {
    ThemeContent(
        darkThemeOverride = null,
        onThemeOptionSelected = {}
    )
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
