package com.example.locator

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var mMap: GoogleMap
	private var mapIsReady: Boolean = false
	private var isResume: Boolean = false
	private var isFirstTime: Boolean = true



	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult) {
			super.onLocationResult(locationResult)

			val userLocation = locationResult.locations[0]

			val lat = userLocation?.latitude
			val lon = userLocation?.longitude

			if (lat != null && lon != null){

				// Removes all markers from the map
				mMap.clear()

				// Add marker in the user's location, and moves the camera to this location.
				val userLocationCoords = LatLng(lat, lon)
				mMap.addMarker(MarkerOptions()
					.position(userLocationCoords)
					.title("This is your location!")
					.icon(bitmapDescriptorFromVector(this@MapActivity , R.drawable.baseline_circle_24)))
				if (isFirstTime) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 11.5f))
					isFirstTime = false
				}
			}
			else{
				Toast.makeText(this@MapActivity, "we couldn't find your location!", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
		return ContextCompat.getDrawable(context, vectorResId)?.run {
			setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
			draw(Canvas(bitmap))
			BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (grantResults.any { it == PackageManager.PERMISSION_GRANTED })
			tryTrackingAfterTheUser()
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)


		// Check for permission
		if (!areLocationPermissionsSet()) {
			ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
		}

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

		setContentView(R.layout.map_activity)

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)


	}



	private fun areLocationPermissionsSet() : Boolean{
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return false
		}
		return true
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap
		mapIsReady = true
		tryTrackingAfterTheUser()
	}


	override fun onResume() {
		super.onResume()
		isResume = true

		tryTrackingAfterTheUser()
	}


	override fun onStop() {
		super.onStop()
		stopTrackingAfterTheUser()
	}



	private fun tryTrackingAfterTheUser(){
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
			mapIsReady && isResume)
			fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper())
	}

	private fun stopTrackingAfterTheUser(){
		fusedLocationClient.removeLocationUpdates(locationCallback)
	}


	private fun getLocationRequest() : LocationRequest {
		val locationRequest = createLocationRequest()
		val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
		val client: SettingsClient = LocationServices.getSettingsClient(this)
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

	private fun createLocationRequest() : LocationRequest{
		val locationRequest = LocationRequest.create().apply {
			interval = 10000
			fastestInterval = 5000
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		}

		return locationRequest
	}




}