package com.locator


import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.lightme.locator.R
import data.LastSeen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import manager.LocationManager
import request.RequestHandler.getServerConnection
import utils.TimeUtils


/**
 * The screen that shows a map with the results of the search of another user.
 */
class MapActivity : AppCompatActivity() {

	// todo: maybe delete?
	private var currentLocationMarker: Marker? = null

	private var otherUserMarker: Marker? = null

	private val viewModel: MapActivityViewModel by viewModels()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.map_activity)

		// Check for permission.
		LocationManager.requestPermissionsIfNeeded(this)

		// Initialize the map.
		initializeMap()
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
	 * Reads the intent that started this activity,
	 * and returns the search query from this intent.
	 */
	private fun readIntent(): String? {
		val intent = intent

		// Verify the action and get the query.
		val query: String? = if (Intent.ACTION_SEARCH == intent.action) {
			intent.getStringExtra(SearchManager.QUERY)
		} else {
			intent.getStringExtra("query")
		}

		return query
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	private fun initializeMap() {
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync {
			Log.d(TAG, "The map is ready!")

			startTrackingTheUser()

			// Read the search query from the intent.
			val query = readIntent()

			// perform search, and presents the results.
			if (!query.isNullOrEmpty()) {
				performSearch(query, it)
			}

			// Add observers to the view model.
			viewModel.userLocation.observe(this) { userLocation -> updateSelfLocationOnMap(userLocation, it) }

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
	 * Perform a location search for the given query (which is a user id),
	 * and show the result on the map.
	 */
	private fun performSearch(query: String, map: GoogleMap) {
		// Remove old marker, if needed.
		otherUserMarker?.remove()

		lifecycleScope.launch {
			val result = getLocationFromServer(query)

			result.fold(
				{ lastSeen -> showOtherUserOnMap(lastSeen, map) },
				{ e -> showError(e) }
			)
		}
	}


	/**
	 * Call the server to get the user location.
	 */
	private suspend fun getLocationFromServer(query: String): Result<LastSeen>{
		return withContext(Dispatchers.IO){
			runCatching {
				val serverConnection = getServerConnection()
				serverConnection.getUserLocation(query)
			}
		}
	}


	/**
	 * Update the ui with the given new location.
	 */
	private fun updateSelfLocationOnMap(userLocation: Location, map: GoogleMap) {
		Log.d(TAG, "update the ui with a new location")


		// todo: This is the old marker that shows the current user location. maybe delete?

		// Removes the last user location from the map.
//		currentLocationMarker?.remove()

		// Add a new marker for the user location.
//		currentLocationMarker = addMarkerToMap(userLocation.latitude, userLocation.longitude, R.drawable.my_location, map)


		if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			var array = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				array += Manifest.permission.ACCESS_BACKGROUND_LOCATION
			}

			ActivityCompat.requestPermissions(this, array, 1)

			return
		}

		map.isMyLocationEnabled = true // This is the blue dot on the map.
	}



	/**
	 * Show a marker on the map in the given lat & lon.
	 */
	private fun showOtherUserOnMap(lastSeen: LastSeen, map: GoogleMap) {
		hideLoader()
		showMap()

		val lat = lastSeen.lat
		val lon = lastSeen.lon

		otherUserMarker = addOtherUserToMap(lastSeen, map)
		showInfoAboveMarker(otherUserMarker)

		// Do camera animation zoom.
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 15f))
	}


	/**
	 * Show an error dialog.
	 */
	private fun showError(e: Throwable){
		Log.e(TAG, "Got exception: $e")
		supportFragmentManager.setFragmentResultListener("dialog_dismissed", this) { _, _ -> finish() }
		ErrorDialog.show(supportFragmentManager)
	}


	/**
	 * Show the map.
	 */
	private fun showMap(){
		val mapFragment: FragmentContainerView = findViewById(R.id.map)
		mapFragment.visibility = FragmentContainerView.VISIBLE
	}


	/**
	 * Hide the loader.
	 */
	private fun hideLoader(){
		val loader: ProgressBar = findViewById(R.id.loader)
		loader.visibility = ProgressBar.INVISIBLE
	}


	/**
	 * Start tracking the user's location.
	 */
	private fun startTrackingTheUser() {
		viewModel.startTracking(this)
	}


	/**
	 * Stop tracking the user's location.
	 */
	private fun stopTrackingTheUser() {
		viewModel.stopTracking()
	}


	/**
	 * Add a marker to the map at the specified location.
	 */
	private fun addOtherUserToMap(lastSeen: LastSeen, map: GoogleMap): Marker? {
		// Add marker in the user's location.
		val userLocationCoordinates = LatLng(lastSeen.lat, lastSeen.lon)
		val time = TimeUtils.convertToDate(lastSeen.timestamp)

		return map.addMarker(MarkerOptions()
			.position(userLocationCoordinates)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
			.title(lastSeen.name)
			.snippet("Last seen: $time")
		)
	}


	/**
	 * Show the info window of the marker.
	 */
	private fun showInfoAboveMarker(marker: Marker?){
		lifecycleScope.launch {
			delay(1000)
			marker?.showInfoWindow()
		}
	}


	companion object {
		val TAG: String
			get() = "MapActivity"


		fun createStartIntent(context: Context, searchQuery: String): Intent {
			val intent = Intent(context, MapActivity::class.java)
			intent.putExtra("query", searchQuery)

			return intent
		}

	}


}