package com.example.locator

import android.location.Location

data class Location(val latitude: Double, val longitude: Double){
	constructor(location: Location) : this(location.latitude, location.longitude)
}
