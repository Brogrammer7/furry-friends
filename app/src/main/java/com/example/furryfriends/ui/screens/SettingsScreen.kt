package com.example.furryfriends.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.SpinningLoader
import com.example.furryfriends.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val owner = LocalViewModelStoreOwner.current
        ?: throw IllegalStateException("No ViewModelStoreOwner available")
    val viewModel: SettingsViewModel = viewModel<SettingsViewModel>(viewModelStoreOwner = owner)

    val zip by viewModel.zip.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    val detectedZipAnnotatedString = buildAnnotatedString {
        append("Your detected ZIP")
        append(": \n")
        withStyle(
            style = if (zip != null) {
                SpanStyle(color = MaterialTheme.colorScheme.primary)
            } else {
                SpanStyle(color = MaterialTheme.colorScheme.error)
            }
        ) {
            append(zip ?: "Not found")
        }
    }

    val granted by viewModel.granted.collectAsState()

    val darkThemeOverride by viewModel.darkThemeOverride.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.settings_disclosure),
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        LocationPermissionSetting(
            viewModel = viewModel,
            granted = granted,
            onGrantedChange = { newGranted -> viewModel.grantPermission(newGranted) }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detecting ZIP…",
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SpinningLoader()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left column constrained with weight so the Retry button has room
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detectedZipAnnotatedString,
                            softWrap = true
                        )
                        message?.let {
                            Text(
                                text = it,
                                softWrap = true,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (granted) {
                        if (zip.isNullOrEmpty()) {
                            TextButton(
                                onClick = { viewModel.fetchZipFromLastLocation() },
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Retry",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            TextButton(
                                onClick = { viewModel.reDetectZip() },
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Re-detect",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

            if (!isSystemInDarkTheme()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Enable Dark Theme", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = darkThemeOverride,
                        onCheckedChange = { checked -> viewModel.setDarkThemeOverride(checked) }
                    )
                }
            }

        }

    }
}

@Composable
fun LocationPermissionSetting(
    viewModel: SettingsViewModel,
    granted: Boolean,
    onGrantedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    requireNotNull(activity) { "The Composable function must be called within an Activity context." }

    // Launcher to request permission
    val requestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onGrantedChange(isGranted)  // Update the hoisted state
        if (isGranted) {
            Toast.makeText(context, "Permission granted — detecting ZIP...", Toast.LENGTH_SHORT).show()
            viewModel.fetchZipFromLastLocation()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher to open app settings
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Refresh state when returning from settings
        viewModel.checkLocationPermission(context)
        if (granted) {
            viewModel.fetchZipFromLastLocation()
        }
    }

    val onClick = {
        when {
            granted -> {
                // Open app settings so user can revoke or re-check location
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                settingsLauncher.launch(intent)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(context, "Location is needed for this feature.", Toast.LENGTH_SHORT).show()
                requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Location access", style = MaterialTheme.typography.bodyLarge)

            Text(
                text = if (granted) "Granted" else "Denied",
                style = MaterialTheme.typography.bodyMedium,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(onClick = onClick) {
            Text(text = if (granted) "Open settings" else "Enable")
        }
    }

    // If permission already granted when composable enters composition, trigger fetch once.
    LaunchedEffect(granted) {
        if (granted) {
            viewModel.fetchZipFromLastLocation()
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
