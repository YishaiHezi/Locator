package data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Represents a user.
 */
data class UserDetails(val name: String, val id: String)


/**
 * Represents a user location.
 */
@Parcelize
data class UserLocation(val lat: Double, val lon: Double) : Parcelable


/**
 * Represents a last seen location of a user.
 */
data class LastSeen(val name: String, val lat: Double, val lon: Double, val timestamp: Long)
