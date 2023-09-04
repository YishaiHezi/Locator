package com.example.locator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task

class MapActivityViewModel(application: Application) : AndroidViewModel(application){


	/**
	 * The mutable location live data.
	 */
	private val userLocationMutable: MutableLiveData<Location> = MutableLiveData()


	/**
	 * The immutable location live data.
	 */
	val userLocation: LiveData<Location> = userLocationMutable


	/**
	 * This is an object that is part of google api. Used to track the user location.
	 */
	private var fusedLocationClient: FusedLocationProviderClient? = null

	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			super.onLocationResult(locationResult)

			if (locationResult.locations.isNotEmpty()) {
				val locations = locationResult.locations
				userLocationMutable.value = locations[locations.size - 1]
			}
		}
	}


	/**
	 * Starts tracking the user's location.
	 */
	@SuppressLint("MissingPermission")
	fun startTracking(activity: Activity){
		if (fusedLocationClient == null)
			fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

		if (permissionGranted(activity))
			fusedLocationClient?.requestLocationUpdates(getLocationRequest(activity), locationCallback, Looper.getMainLooper())
	}


	/**
	 * Stop tracking the user's location.
	 */
	fun stopTracking(){
		fusedLocationClient?.removeLocationUpdates(locationCallback)
	}


	/**
	 * Location permissions were granted by the user.
	 */
	private fun permissionGranted(activity: Activity): Boolean{
		return ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}


	/**
	 * Builds a location request, which holds all the needed parameters that we need.
	 */
	private fun getLocationRequest(activity: Activity) : LocationRequest {
		val locationRequest = LocationRequest.Builder(10000).build()
		val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
		val client: SettingsClient = LocationServices.getSettingsClient(activity)
		val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

		task.addOnFailureListener { exception ->
			if (exception is ResolvableApiException){
				// Location settings are not satisfied, but this can be fixed
				// by showing the user a dialog.
				try {
					// Show the dialog by calling startResolutionForResult(),
					// and check the result in onActivityResult().
//					exception.startResolutionForResult(this@MapActivity, )
				} catch (sendEx: IntentSender.SendIntentException) {
					// Ignore the error.
				}
			}
		}

		return locationRequest
	}





}