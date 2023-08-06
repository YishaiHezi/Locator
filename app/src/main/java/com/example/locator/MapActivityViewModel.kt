package com.example.locator

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


	private val userLocationMutable: MutableLiveData<Location> = MutableLiveData()
	val userLocation: LiveData<Location> = userLocationMutable


	private var fusedLocationClient: FusedLocationProviderClient? = null

	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			super.onLocationResult(locationResult)

			userLocationMutable.value = locationResult.locations[0]
		}
	}



	fun startTracking(activity: Activity){
		if (fusedLocationClient == null)
			fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

		if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
			fusedLocationClient?.requestLocationUpdates(getLocationRequest(activity), locationCallback, Looper.getMainLooper())
	}
	fun stopTracking(){
		fusedLocationClient?.removeLocationUpdates(locationCallback)
	}



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