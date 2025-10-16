package dev.abbasian.protocol

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * A single GPS location point with all the info we care about.
 * Parcelable so we can pass it between processes efficiently.
 */
@Parcelize
data class LocationData(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    /** When this location was recorded */
    val timestamp: Long,
    /** Where it came from - could be "gps", "network", "fused", etc. */
    val provider: String,
) : Parcelable {
    /**
     * Converts the timestamp into something humans can actually read
     */
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Gives you the coordinates as a nice readable string
     */
    fun getCoordinatesString(): String {
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }

    /**
     * Quick sanity check - makes sure the location data isn't garbage
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            accuracy >= 0f &&
            timestamp > 0L
    }

    /**
     * Tells you how old this location is in milliseconds
     */
    fun getAge(): Long {
        return System.currentTimeMillis() - timestamp
    }

    /**
     * Returns true if the location is less than 5 minutes old
     */
    fun isFresh(): Boolean {
        return getAge() < 5 * 60 * 1000 // 5 minutes
    }

    override fun toString(): String {
        return "LocationData(id='$id', lat=$latitude, lon=$longitude, " +
            "accuracy=${accuracy}m, time=${getFormattedDate()}, provider='$provider')"
    }

    companion object {
        fun createSample(): LocationData {
            return LocationData(
                latitude = 37.7749,
                longitude = -122.4194,
                accuracy = 10.0f,
                timestamp = System.currentTimeMillis(),
                provider = "gps",
            )
        }
    }
}
