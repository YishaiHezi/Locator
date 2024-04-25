package data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserLocation(val lat: Double, val lon: Double) : Parcelable
