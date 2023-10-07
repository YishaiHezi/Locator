package com.locator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserLocation(val latitude: Double, val longitude: Double) : Parcelable
