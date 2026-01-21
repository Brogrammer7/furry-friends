package com.example.furryfriends.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.App
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SettingsViewModel"

    // repository using application context (no DI)
    private val repository = (application as App).settingsRepository

    private val _zip = MutableStateFlow<String?>(null)
    val zip: StateFlow<String?> = _zip.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _darkThemeEnabled = MutableStateFlow(false)
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled.asStateFlow()

    init {
        // Initialize dark theme state from repository and keep it in sync
        viewModelScope.launch {
            // Read current persisted value once
            _darkThemeEnabled.value = repository.isDarkThemeEnabled()
            // Also collect ongoing updates (if any other writer exists)
            launch {
                repository.darkThemeEnabled.collectLatest { _darkThemeEnabled.value = it }
            }
        }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        _darkThemeEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setDarkThemeEnabled(enabled)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist dark theme setting", e)
            }
        }
    }

    /**
     * Public entry: call only when coarse location permission is granted.
     */
    @SuppressLint("MissingPermission")
    fun fetchZipFromLastLocation() {
        val appCtx: Context = getApplication<Application>().applicationContext
        val perm = android.Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(appCtx, perm) != PackageManager.PERMISSION_GRANTED) {
            _message.value = "Location permission not granted."
            _zip.value = null
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _message.value = null
            try {
                val fused = LocationServices.getFusedLocationProviderClient(appCtx)

                // Try cached lastLocation, retry once after short delay
                var loc = getLastLocationSuspend(fused)
                if (loc == null) {
                    Log.d(TAG, "lastLocation was null, retrying after delay")
                    delay(500)
                    loc = getLastLocationSuspend(fused)
                }

                // Try getCurrentLocation as next fallback
                if (loc == null) {
                    Log.d(TAG, "Attempting getCurrentLocation fallback")
                    loc = getCurrentLocationSuspend(fused)
                }

                // If still null, try active request with updates for up to 8s
                if (loc == null) {
                    Log.d(TAG, "Attempting active request fallback")
                    loc = getLocationViaRequest(fused, timeoutMs = 8_000)
                }

                if (loc == null) {
                    Log.w(TAG, "No recent location available")
                    _message.value = "No recent location available. Please enable device Location or try again."
                    _zip.value = null
                } else {
                    Log.d(TAG, "Got location: lat=${loc.latitude}, lon=${loc.longitude}")
                    val postal = reverseGeocodeToZip(loc.latitude, loc.longitude, appCtx)
                    if (!postal.isNullOrEmpty()) {
                        _zip.value = postal
                        _message.value = null
                        Log.d(TAG, "Resolved postal code: $postal")
                    } else {
                        Log.w(TAG, "Could not resolve postal code from location")
                        _message.value = "Could not resolve postal code from location."
                        _zip.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving location", e)
                _message.value = "Error retrieving location."
                _zip.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun getLastLocationSuspend(
        fused: FusedLocationProviderClient
    ): Location? = withContext(Dispatchers.Main) {
        val appCtx = getApplication<Application>().applicationContext
        if (ContextCompat.checkSelfPermission(appCtx, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"permission missing before lastLocation")
            return@withContext null
        }
        suspendCancellableCoroutine { cont ->
            try {
                fused.lastLocation
                    .addOnSuccessListener { loc -> if (cont.isActive) cont.resumeWith(Result.success(loc)) }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "lastLocation failure", e)
                        if (cont.isActive) cont.resumeWith(Result.success(null))
                    }
            } catch (se: SecurityException) {
                Log.w(TAG,"SecurityException calling lastLocation", se)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            } catch (e: Exception) {
                Log.w(TAG, "Exception calling lastLocation", e)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            }
        }
    }

    private suspend fun getCurrentLocationSuspend(
        fused: FusedLocationProviderClient
    ): Location? = withContext(Dispatchers.Main) {
        // Try getCurrentLocation first (balanced power)
        val direct = suspendCancellableCoroutine<Location?> { cont ->
            try {
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc -> if (cont.isActive) cont.resumeWith(Result.success(loc)) }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "getCurrentLocation failure", e)
                        if (cont.isActive) cont.resumeWith(Result.success(null))
                    }
                cont.invokeOnCancellation { cts.cancel() }
            } catch (se: SecurityException) {
                Log.w(TAG,"SecurityException calling lastLocation", se)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            } catch (e: Exception) {
                Log.w(TAG, "Exception calling getCurrentLocation", e)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            }
        }
        if (direct != null) return@withContext direct

        // Fallback to active request
        getLocationViaRequest(fused, timeoutMs = 8_000)
    }

    private suspend fun getLocationViaRequest(
        fused: FusedLocationProviderClient,
        timeoutMs: Long = 8_000
    ): Location? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                0
            )
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdateDelayMillis(0)
                .build()

            val callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val loc = result.lastLocation
                    if (loc != null && cont.isActive) {
                        cont.resumeWith(Result.success(loc))
                        try { fused.removeLocationUpdates(this) } catch (_: Exception) {}
                    }
                }
            }

            try {
                fused.requestLocationUpdates(locationRequest, callback, android.os.Looper.getMainLooper())
                    .addOnFailureListener { e -> Log.w(TAG, "requestLocationUpdates failure", e) }

                // Timeout: remove updates after timeoutMs and resume null if still waiting
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                val timeoutRunnable = Runnable {
                    if (cont.isActive) {
                        cont.resumeWith(Result.success(null))
                        try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
                    }
                }
                handler.postDelayed(timeoutRunnable, timeoutMs)

                cont.invokeOnCancellation {
                    try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
                    handler.removeCallbacks(timeoutRunnable)
                }
            } catch (se: SecurityException) {
                Log.w(TAG,"SecurityException calling lastLocation", se)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            } catch (e: Exception) {
                Log.w(TAG, "Exception requesting location updates", e)
                if (cont.isActive) cont.resumeWith(Result.success(null))
            }
        }
    }

    private suspend fun reverseGeocodeToZip(lat: Double, lon: Double, context: Context): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                return@withContext if (android.os.Build.VERSION.SDK_INT >= 33) {
                    // async listener API (API 33+)
                    suspendCancellableCoroutine { cont ->
                        val listener = object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                if (cont.isActive) cont.resumeWith(Result.success(addresses.firstOrNull()?.postalCode))
                            }
                        }
                        geocoder.getFromLocation(lat, lon, 1, listener)
                        cont.invokeOnCancellation { /* no cancel API */ }
                    }
                } else {
                    // fallback for older devices: run blocking call on IO thread
                    val results = geocoder.getFromLocation(lat, lon, 1)
                    results?.firstOrNull()?.postalCode
                }
            } catch (e: Exception) {
                Log.w(TAG, "Geocoder failed", e)
                null
            }
        }

    fun saveManualZip(manual: String) {
        _zip.value = manual.trim().takeIf { it.isNotEmpty() }
    }

    fun isCoarsePermissionGranted(): Boolean {
        val appCtx = getApplication<Application>().applicationContext
        val perm = android.Manifest.permission.ACCESS_COARSE_LOCATION
        return ContextCompat.checkSelfPermission(appCtx, perm) == PackageManager.PERMISSION_GRANTED
    }
}
