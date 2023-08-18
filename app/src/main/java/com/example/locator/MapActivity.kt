package com.example.locator

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.viewModels
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity() {

	private var mMap: GoogleMap? = null
	private var mapIsReady: Boolean = false


	private val viewModel: MapActivityViewModel by viewModels()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.map_activity)

		// Check for permission.
		requestPermissionsIfNeeded()

		// Initialize the map.
		initializeMap()

		// Add observers to the view model.
		addLocationObservers()
	}


	override fun onResume() {
		super.onResume()
		startTrackingTheUser()
	}

	override fun onPause() {
		super.onPause()
		stopTrackingTheUser()
	}


	/**
	 * Request the required permissions from the user.
	 */
	private fun requestPermissionsIfNeeded(){
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
			return
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (grantResults.any { it == PackageManager.PERMISSION_GRANTED })
			startTrackingTheUser()
		else
			Log.d(TAG, "permission wasn't granted by the user")
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
	private fun initializeMap(){
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync{
			Log.d(TAG, "The map is ready!")
			mMap = it
			mapIsReady = true
			startTrackingTheUser()
		}
	}


	/**
	 * Start tracking the user's location.
	 */
	private fun startTrackingTheUser(){
		viewModel.startTracking(this)
	}


	/**
	 * Stop tracking the user's location.
	 */
	private fun stopTrackingTheUser(){
		viewModel.stopTracking()
	}


	/**
	 * Add the observers to the view model.
	 */
	private fun addLocationObservers(){
		// Observer for location updates.
		viewModel.userLocation.observe(this) { userLocation -> updateUi(userLocation) }

		// One time observer for the animation of the camera.
		viewModel.userLocation.observe(this, object : Observer<Location>{
			override fun onChanged(value: Location) {
				animateCamera(value)
				viewModel.userLocation.removeObserver(this)
			}
		})
	}


	/**
	 * Update the ui with the given new location.
	 */
	private fun updateUi(userLocation: Location){
		if (!mapIsReady)
			return

		Log.d(TAG, "update the ui with a new location")

		// Removes all markers from the map
		mMap?.clear()

		val lat = userLocation.latitude
		val lon = userLocation.longitude

		// Add marker in the user's location, and moves the camera to this location.
		val userLocationCoordinates = LatLng(lat, lon)
		mMap?.addMarker(MarkerOptions()
			.position(userLocationCoordinates)
			.title("This is your location!")
			.icon(bitmapDescriptorFromVector(this@MapActivity , R.drawable.baseline_circle_24)))
	}


	/**
	 * Animate the camera (changes the zoom from a low zoom to a high).
	 */
	private fun animateCamera(userLocation: Location){
		if(!mapIsReady)
			return

		val lat = userLocation.latitude
		val lon = userLocation.longitude
		mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 11.5f))
	}



	private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
		return ContextCompat.getDrawable(context, vectorResId)?.run {
			setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
			draw(Canvas(bitmap))
			BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}


	companion object{
		val TAG: String
			get() = "MapActivity"
	}




}