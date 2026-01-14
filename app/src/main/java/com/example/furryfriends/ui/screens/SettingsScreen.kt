package com.example.furryfriends.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.provider.Settings
import android.net.Uri
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.furryfriends.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.example.furryfriends.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val zip by viewModel.zip.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    Scaffold(
        topBar = {
            FurryFriendsAppBar(stringResource(R.string.settings_screen_title))
        }
    ) { innerPadding ->
        Column(modifier = modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.settings_disclosure),
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(16.dp)
            )
            LocationPermissionSetting(viewModel = viewModel)

            Column(modifier = Modifier.padding(16.dp)) {
                if (loading) {
                    Text(text = "Detecting zip…")
                } else {
                    Text(text = "Zip: ${zip ?: "Not set"}")
                    message?.let {
                        Text(text = it, color = Color(0xFFB00020))
                    }
                }

                // Retry button shown when not loading and zip not set
                if (!loading && zip.isNullOrEmpty()) {
                    Button(onClick = { viewModel.fetchZipFromLastLocation() }, modifier = Modifier.padding(top = 12.dp)) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationPermissionSetting(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permission = android.Manifest.permission.ACCESS_COARSE_LOCATION
    val activity = context as android.app.Activity

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Observe zip so UI can show detected value inline
    val zip by viewModel.zip.collectAsState()

    // Launcher to request permission
    val requestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
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
        granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.fetchZipFromLastLocation()
        }
    }

    val onClick = {
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Open app settings so user can revoke or re-check location
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                settingsLauncher.launch(intent)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                Toast.makeText(context, "Location is needed for this feature.", Toast.LENGTH_SHORT).show()
                requestLauncher.launch(permission)
            }
            else -> {
                requestLauncher.launch(permission)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Location access", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (granted) "Allowed" else "Not allowed",
                style = MaterialTheme.typography.bodySmall,
                color = if (granted) Color(0xFF2E7D32) else Color(0xFFB00020)
            )
            // Show detected zip if available
            zip?.let {
                Text(text = "Detected ZIP: $it", style = MaterialTheme.typography.bodySmall)
            }
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
