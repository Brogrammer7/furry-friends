package com.example.furryfriends.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SettingsViewModel"

    private val _zip = MutableStateFlow<String?>(null)
    val zip: StateFlow<String?> = _zip

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    /**
     * Public entry: call only when coarse location permission is granted.
     */
    @SuppressLint("MissingPermission")
    fun fetchZipFromLastLocation() {
        val appCtx: Context = getApplication<Application>().applicationContext
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
        fused: com.google.android.gms.location.FusedLocationProviderClient
    ): android.location.Location? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            try {
                fused.lastLocation
                    .addOnSuccessListener { cont.resume(it) {} }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "lastLocation failure", e)
                        cont.resume(null) {}
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Exception calling lastLocation", e)
                cont.resume(null) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationSuspend(
        fused: com.google.android.gms.location.FusedLocationProviderClient
    ): android.location.Location? = withContext(Dispatchers.Main) {
        // Try getCurrentLocation first (balanced power)
        val direct = suspendCancellableCoroutine<android.location.Location?> { cont ->
            try {
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { cont.resume(it) {} }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "getCurrentLocation failure", e)
                        cont.resume(null) {}
                    }
                cont.invokeOnCancellation { cts.cancel() }
            } catch (e: Exception) {
                Log.w(TAG, "Exception calling getCurrentLocation", e)
                cont.resume(null) {}
            }
        }
        if (direct != null) return@withContext direct

        // Fallback to active request
        getLocationViaRequest(fused, timeoutMs = 8_000)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationViaRequest(
        fused: com.google.android.gms.location.FusedLocationProviderClient,
        timeoutMs: Long = 8_000
    ): android.location.Location? = withContext(Dispatchers.Main) {
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
                        cont.resume(loc) {}
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
                        cont.resume(null) {}
                        try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
                    }
                }
                handler.postDelayed(timeoutRunnable, timeoutMs)

                cont.invokeOnCancellation {
                    try { fused.removeLocationUpdates(callback) } catch (_: Exception) {}
                    handler.removeCallbacks(timeoutRunnable)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception requesting location updates", e)
                cont.resume(null) {}
            }
        }
    }

    private suspend fun reverseGeocodeToZip(lat: Double, lon: Double, context: Context): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocation(lat, lon, 1) ?: return@withContext null
                results.firstOrNull()?.postalCode
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
