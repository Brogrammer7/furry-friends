package com.example.furryfriends.features.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.furryfriends.LoginActivity
import com.example.furryfriends.R
import com.example.furryfriends.ui.components.SignOutButton
import com.example.furryfriends.ui.components.SpinningLoader

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onNavigateToTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val zip by viewModel.zip.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    val detectedZipAnnotatedString = buildAnnotatedString {
        append(stringResource(R.string.your_detected_zip))
        append("\n")
        withStyle(
            style = if (zip != null) {
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            } else {
                SpanStyle(
                    color = MaterialTheme.colorScheme.error,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
        ) {
            append(zip ?: stringResource(R.string.not_set))
        }
    }

    val granted by viewModel.granted.collectAsState()

    val darkThemeOverride by viewModel.darkThemeOverride.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "System Theme", style = MaterialTheme.typography.bodyLarge)

                val themeName = when(darkThemeOverride) {
                    true -> "Dark"
                    false -> "Light"
                    null -> "System Default"
                }

                Text(
                    text = themeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = onNavigateToTheme,
            ) {
                Text(
                    text = stringResource(R.string.change_theme),
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        LocationPermissionSetting(
            viewModel = viewModel,
            granted = granted,
            onGrantedChange = { newGranted -> viewModel.grantPermission(newGranted) }
        )

        @Composable
        fun ZipDetectionRow(content: @Composable RowScope.() -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        if (loading && zip == null) {
            ZipDetectionRow {
                Text(
                    text = stringResource(R.string.detecting_zip),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.weight(1f))
                SpinningLoader()
            }
        } else {
            ZipDetectionRow {
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
                TextButton(onClick = { viewModel.fetchZipFromLastLocation() }) {
                    Text("Re-detect")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                fun signOutAction() {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

                SignOutButton(onSignOut = { signOutAction() })
            }

            Text(
                text = stringResource(R.string.settings_disclosure),
                style = TextStyle(fontSize = 12.sp),
            )
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
            if (viewModel.zip != null) viewModel.fetchZipFromLastLocation()
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
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = stringResource(R.string.location_access), style = MaterialTheme.typography.bodyLarge)

            Text(
                text = if (granted) stringResource(R.string.granted) else stringResource(R.string.disabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(onClick = onClick) {
            Text(
                text = if (granted) stringResource(R.string.open_settings) else stringResource(R.string.enable),
                textAlign = TextAlign.Center
            )
        }
    }

    // If permission already granted when composable enters composition, trigger fetch once.
    LaunchedEffect(granted) {
        if (granted) {
            viewModel.fetchZipFromLastLocation()
        }
    }
}