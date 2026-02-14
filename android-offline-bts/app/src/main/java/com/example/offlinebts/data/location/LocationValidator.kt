package com.example.offlinebts.data.location

import android.location.Location
import kotlin.math.abs

data class LocationValidation(
    val trusted: Boolean,
    val reason: String
)

class LocationValidator {

    fun validate(current: Location, previous: Location?): LocationValidation {
        if (current.isFromMockProvider) {
            return LocationValidation(false, "Mock provider detected")
        }

        if (current.accuracy > 40f) {
            return LocationValidation(false, "Low GPS accuracy: ${current.accuracy}m")
        }

        if (previous != null) {
            val dtSec = (current.time - previous.time) / 1000.0
            if (dtSec > 0) {
                val speed = previous.distanceTo(current) / dtSec
                if (speed > 60.0) {
                    return LocationValidation(false, "Unrealistic jump speed=${"%.1f".format(speed)} m/s")
                }
            }
        }

        val vertical = if (current.hasVerticalAccuracy()) current.verticalAccuracyMeters else 0f
        if (abs(vertical) > 100f) {
            return LocationValidation(false, "Vertical accuracy outlier")
        }

        return LocationValidation(true, "GPS looks plausible")
    }
}
