package org.example.project

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import java.util.Locale

@SuppressLint("MissingPermission")
fun getDeviceLocation(context: Context): LocationData {
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER
    )

    for (provider in providers) {
        val location = locationManager.getLastKnownLocation(provider)

        if (location != null) {
            val geocoder = Geocoder(context, Locale.getDefault())

            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            val city = addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.adminArea
                ?: "Unknown"

            return LocationData(
                location.latitude,
                location.longitude,
                city
            )
        }
    }

    // fallback (only if location fails)
    return LocationData(28.61, 77.20, "Delhi")
}