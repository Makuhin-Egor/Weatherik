package com.example.weatherapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val lastLocation = suspendCancellableCoroutine<Location?> { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location -> cont.resume(location) }
                    .addOnFailureListener { cont.resume(null) }
            }

            if (lastLocation != null && (System.currentTimeMillis() - lastLocation.time < 300000)) {
                return lastLocation
            }

            suspendCancellableCoroutine { cont ->
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_LOW_POWER,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location -> cont.resume(location) }
                    .addOnFailureListener { cont.resume(null) }
            }
        } catch (e: Exception) {
            null
        }
    }
}
