package com.example.lanchat.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.example.lanchat.network.MessageType
import com.example.lanchat.network.WireMessage

/**
 * Captures the device's current GPS coordinates and packages them as a
 * WireMessage(type = LOCATION) to send over the chat channel.
 *
 * Note on maps offline: this class only produces raw lat/lng -- it works
 * fully offline (GPS doesn't need internet or even cell signal). What
 * WON'T work offline is rendering a visual map *background* behind the pin,
 * since that normally streams tiles from Google Maps/OpenStreetMap. Two
 * options for the receiving side's UI: (a) just show coordinates + a
 * compass bearing/distance from the recipient's own position, or (b) bundle
 * a pre-downloaded offline tile pack for your factory's local area if a
 * real map background matters.
 */
class LocationSharer(private val context: Context) {

    @SuppressLint("MissingPermission") // caller is responsible for requesting ACCESS_COARSE_LOCATION first
    fun getCurrentLocationMessage(myPeerId: String, toPeerId: String?): WireMessage? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }

        val lastLocation = locationManager.getLastKnownLocation(provider) ?: return null

        return WireMessage(
            type = MessageType.LOCATION,
            fromPeerId = myPeerId,
            toPeerId = toPeerId,
            latitude = lastLocation.latitude,
            longitude = lastLocation.longitude
        )
    }
}
