package com.example.furryfriends.features.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.furryfriends.R
import com.example.furryfriends.data.repository.SettingsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    private val TAG = "SettingsViewModel"

    private val _granted = MutableStateFlow(false)
    val granted: StateFlow<Boolean> = _granted.asStateFlow()

    private val _zip = MutableStateFlow<String?>(null)
    val zip: StateFlow<String?> = _zip.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _darkThemeOverride = MutableStateFlow<Boolean?>(null)
    val darkThemeOverride: StateFlow<Boolean?> = _darkThemeOverride.asStateFlow()

    // Manual Entry States
    private val _manualZipInput = MutableStateFlow("")
    val manualZipInput: StateFlow<String> = _manualZipInput.asStateFlow()

    private val _showManualInput = MutableStateFlow(false)
    val showManualInput: StateFlow<Boolean> = _showManualInput.asStateFlow()

    private val _inputError = MutableStateFlow(false)
    val inputError: StateFlow<Boolean> = _inputError.asStateFlow()

    // Login state initialized to null to prevent accidental redirects while loading from DataStore
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        // Check initial permission state
        checkLocationPermission(applicationContext)

        // Initialize dark theme state from repository and keep it in sync
        viewModelScope.launch {
            // Read current persisted value once
            _darkThemeOverride.value = repository.isDarkThemeOverride()
            // Also collect ongoing updates (if any other writer exists)
            launch {
                repository.darkThemeOverride.collectLatest { _darkThemeOverride.value = it }
            }
        }

        // Initialize zip code from repository and keep it in sync
        viewModelScope.launch {
            // Load persisted zip on init
            _zip.value = repository.getZip()
            // Sync ongoing updates from repository
            launch {
                repository.zip.collectLatest { _zip.value = it }
            }
        }

        // Initialize login state
        viewModelScope.launch {
            _isLoggedIn.value = repository.getIsLoggedIn()
            launch {
                repository.isLoggedIn.collectLatest { _isLoggedIn.value = it }
            }
        }
    }

    /**
     * Set theme preference.
     * @param enabled true for dark, false for light, null for system default
     */
    fun setDarkThemeOverride(enabled: Boolean?) {
        _darkThemeOverride.value = enabled
        viewModelScope.launch {
            try {
                repository.setDarkThemeOverride(enabled)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist dark theme setting", e)
            }
        }
    }

    fun checkLocationPermission(context: Context) {
        _granted.value = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun grantPermission(isGranted: Boolean) {
        _granted.value = isGranted
    }

    /**
     * Public entry: call only when fine location permission is granted.
     */
    @SuppressLint("MissingPermission")
    fun fetchZipFromLastLocation() {
        if (!granted.value) {
            _message.value = applicationContext.getString(R.string.location_permission_not_granted)
            _zip.value = null
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _message.value = null
            try {
                val fused = LocationServices.getFusedLocationProviderClient(applicationContext)

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
                    _message.value = applicationContext.getString(R.string.no_location_available)
                    _zip.value = null
                } else {
                    Log.d(TAG, "Got location: lat=${loc.latitude}, lon=${loc.longitude}")
                    val postal = reverseGeocodeToZip(loc.latitude, loc.longitude, applicationContext)
                    if (!postal.isNullOrEmpty()) {
                        try {
                            repository.setZip(postal)
                            _zip.value = postal
                            _message.value = null
                            Log.d(TAG, "Resolved postal code: $postal")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to persist zip code", e)
                            _zip.value = null
                        }
                    } else {
                        Log.w(TAG, "Could not resolve postal code from location")
                        _message.value = applicationContext.getString(R.string.could_not_resolve_zip)
                        _zip.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving location", e)
                _message.value = applicationContext.getString(R.string.error_retrieving_location)
                _zip.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun getLastLocationSuspend(
        fused: FusedLocationProviderClient
    ): Location? = withContext(Dispatchers.Main) {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "permission missing before lastLocation")
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
                Log.w(TAG, "SecurityException calling lastLocation", se)
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
                Log.w(TAG, "SecurityException calling lastLocation", se)
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
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                0
            )
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdateDelayMillis(0)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation
                    if (loc != null && cont.isActive) {
                        cont.resumeWith(Result.success(loc))
                        try { fused.removeLocationUpdates(this) } catch (_: Exception) {}
                    }
                }
            }

            try {
                fused.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                    .addOnFailureListener { e -> Log.w(TAG, "requestLocationUpdates failure", e) }

                // Timeout: remove updates after timeoutMs and resume null if still waiting
                val handler = Handler(Looper.getMainLooper())
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
                Log.w(TAG, "SecurityException calling lastLocation", se)
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

    fun reDetectZip() {
        if (!granted.value) {
            _message.value = applicationContext.getString(R.string.location_permission_not_granted)
            _zip.value = null
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _message.value = null
            try {
                val fused = LocationServices.getFusedLocationProviderClient(applicationContext)

                // Try getCurrentLocation first
                var loc = getCurrentLocationSuspend(fused)

                // If current location is null, try active request
                if (loc == null) {
                    loc = getLocationViaRequest(fused, timeoutMs = 8_000)
                }

                if (loc == null) {
                    _message.value = applicationContext.getString(R.string.no_location_available)
                    _zip.value = null
                } else {
                    val postal = reverseGeocodeToZip(loc.latitude, lon = loc.longitude, context = applicationContext)
                    if (!postal.isNullOrEmpty()) {
                        try {
                            repository.setZip(postal)
                            _zip.value = postal
                            _message.value = null
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to persist zip code", e)
                            _zip.value = null
                        }
                    } else {
                        _message.value = applicationContext.getString(R.string.could_not_resolve_zip)
                        _zip.value = null
                    }
                }
            } catch (e: Exception) {
                _message.value = applicationContext.getString(R.string.error_retrieving_location)
                _zip.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun onManualZipChange(input: String) {
        val filtered = input.filter { it.isDigit() }.take(5)
        _manualZipInput.value = filtered
        _inputError.value = false
        
        if (filtered.length == 5) {
            if (isValidZip(filtered)) {
                saveManualZip(filtered)
                _showManualInput.value = false
                _manualZipInput.value = ""
                _inputError.value = false
                _message.value = null
            } else {
                _inputError.value = true
            }
        }
    }

    fun setShowManualInput(show: Boolean) {
        _showManualInput.value = show
        if (!show) {
            _manualZipInput.value = ""
            _inputError.value = false
        }
    }

    fun login() {
        viewModelScope.launch {
            repository.setIsLoggedIn(true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.setIsLoggedIn(false)
        }
    }

    private fun saveManualZip(manual: String) {
        _zip.value = manual
        viewModelScope.launch {
            try {
                repository.setZip(manual)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist zip code", e)
            }
        }
    }

    private fun isValidZip(zip: String): Boolean {
        val asInt = zip.toIntOrNull() ?: return false
        return asInt in 10000..99999
    }
}
