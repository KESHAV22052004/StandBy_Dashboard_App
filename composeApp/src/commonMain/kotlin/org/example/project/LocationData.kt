package org.example.project

import androidx.compose.runtime.compositionLocalOf

data class LocationData(
    val lat: Double = 28.61,
    val lng: Double = 77.20,
    val city: String = "Delhi"
)

val LocalLocationData = compositionLocalOf { LocationData() }