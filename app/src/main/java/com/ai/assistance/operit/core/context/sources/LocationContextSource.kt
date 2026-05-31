package com.ai.assistance.operit.core.context.sources

import android.content.Context
import android.location.LocationManager
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object LocationContextSource {
    fun collect(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            val lastKnown = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                @Suppress("DEPRECATION")
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            return lastKnown?.let { loc ->
                "Location: lat=${String.format("%.4f", loc.latitude)}, lon=${String.format("%.4f", loc.longitude)}, accuracy=${String.format("%.0f", loc.accuracy)}m"
            }
        } catch (e: Exception) {
            return null
        }
    }
}
