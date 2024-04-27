package data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Represents a user.
 */
data class User(val name: String, val id: String)


/**
 * Represents a user location.
 */
@Parcelize
data class UserLocation(val lat: Double, val lon: Double) : Parcelable
