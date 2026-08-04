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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
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
    val activity = context as? Activity
    val zip by viewModel.zip.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val granted by viewModel.granted.collectAsState()
    val darkThemeOverride by viewModel.darkThemeOverride.collectAsState()
    val manualZipInput by viewModel.manualZipInput.collectAsState()
    val showManualInput by viewModel.showManualInput.collectAsState()
    val inputError by viewModel.inputError.collectAsState()

    val permissionGrantedMsg = stringResource(R.string.permission_granted_detecting)
    val permissionDeniedMsg = stringResource(R.string.permission_denied)
    val locationNeededMsg = stringResource(R.string.location_needed)

    // Launcher to request permission
    val requestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.grantPermission(isGranted)
        if (isGranted) {
            Toast.makeText(context, permissionGrantedMsg, Toast.LENGTH_SHORT).show()
            viewModel.fetchZipFromLastLocation()
        } else {
            Toast.makeText(context, permissionDeniedMsg, Toast.LENGTH_SHORT).show()
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

    val onLocationAction = {
        if (activity != null) {
            when {
                granted -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    settingsLauncher.launch(intent)
                }
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(context, locationNeededMsg, Toast.LENGTH_SHORT).show()
                    requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                else -> {
                    requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    SettingsContent(
        modifier = modifier,
        zip = zip,
        loading = loading,
        message = message,
        granted = granted,
        darkThemeOverride = darkThemeOverride,
        manualZipInput = manualZipInput,
        showManualInput = showManualInput,
        inputError = inputError,
        onNavigateToTheme = onNavigateToTheme,
        onFetchZip = {
            if (granted) {
                viewModel.fetchZipFromLastLocation()
            } else {
                onLocationAction()
            }
        },
        onLocationAction = onLocationAction,
        onManualZipChange = { viewModel.onManualZipChange(it) },
        onSetShowManualInput = { viewModel.setShowManualInput(it) }
    )
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    zip: String?,
    loading: Boolean,
    message: String?,
    granted: Boolean,
    darkThemeOverride: Boolean?,
    manualZipInput: String,
    showManualInput: Boolean,
    inputError: Boolean,
    onNavigateToTheme: () -> Unit,
    onFetchZip: () -> Unit,
    onLocationAction: () -> Unit,
    onManualZipChange: (String) -> Unit,
    onSetShowManualInput: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val savedZipAnnotatedString = buildAnnotatedString {
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
                Text(text = stringResource(R.string.system_theme), style = MaterialTheme.typography.bodyLarge)

                val themeName = when(darkThemeOverride) {
                    true -> stringResource(R.string.dark)
                    false -> stringResource(R.string.light)
                    null -> stringResource(R.string.system_default)
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
            granted = granted,
            onLocationAction = onLocationAction,
            onFetchZip = onFetchZip
        )

        if (loading && zip == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detecting_zip),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.weight(1f))
                SpinningLoader()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = savedZipAnnotatedString,
                        softWrap = true
                    )
                    message?.let {
                        Text(
                            text = it,
                            softWrap = true,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = {
                        onFetchZip()
                        onSetShowManualInput(false)
                    }) {
                        Text(stringResource(R.string.re_detect))
                    }
                    if (!showManualInput) {
                        TextButton(onClick = {
                            onSetShowManualInput(true)
                        }) {
                            Text(stringResource(R.string.manual_entry))
                        }
                    }
                }
            }
        }

        if (showManualInput) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = manualZipInput,
                    onValueChange = onManualZipChange,
                    label = { Text(stringResource(R.string.enter_zip)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = inputError
                )
                TextButton(
                    onClick = {
                        onSetShowManualInput(false)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                if (inputError) {
                    Text(
                        text = stringResource(R.string.invalid_zip_retry),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
    granted: Boolean,
    onLocationAction: () -> Unit,
    onFetchZip: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        Button(onClick = onLocationAction) {
            Text(
                text = if (granted) stringResource(R.string.open_settings) else stringResource(R.string.enable),
                textAlign = TextAlign.Center
            )
        }
    }

    // If permission already granted when composable enters composition, trigger fetch once.
    LaunchedEffect(granted) {
        if (granted) {
            onFetchZip()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        zip = "90210",
        loading = false,
        message = null,
        granted = true,
        darkThemeOverride = false,
        manualZipInput = "",
        showManualInput = true,
        inputError = false,
        onNavigateToTheme = {},
        onFetchZip = {},
        onLocationAction = {},
        onManualZipChange = {},
        onSetShowManualInput = {}
    )
}
