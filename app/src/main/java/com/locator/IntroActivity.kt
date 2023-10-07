package com.locator


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.lightme.locator.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import request.RequestHandler.getServerConnection


/**
 * The first screen that will be presented to the user.
 */
class IntroActivity : AppCompatActivity() {

	private var mMap: GoogleMap? = null
	private var mapIsReady: Boolean = false
	private var currentLocationMarker: Marker? = null
	private var resultMarker: Marker? = null


	private val viewModel: IntroActivityViewModel by viewModels()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.intro_activity)

		// Check for permission.
		requestPermissionsIfNeeded()

		// Initialize the ui.
		initializeUi()

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
	private fun requestPermissionsIfNeeded() {
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
	 * Initialize the ui: the tool bar and the map.
	 */
	private fun initializeUi() {
		// Initialize the toolbar.
		initializeToolbar()

		// Initialize the map.
		initializeMap()
	}


	/**
	 * Initialize the tool bar with the search view.
	 */
	private fun initializeToolbar() {
		val presenter = object : LocationPresenter {
			override fun show(lat: Double, lon: Double) {
				resultMarker = addMarkerToMap(lat, lon)
				animateCamera(lat, lon)
			}
		}

		val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
		setSupportActionBar(myToolbar)

		val searchView: SearchView = findViewById(R.id.search_view)

		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				// Remove old marker, if needed.
				resultMarker?.remove()
				showUserLocation(query, presenter)
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean {
				// Handle query text change
				return true
			}
		})



		searchView.setOnCloseListener {
			// Remove the last marker that was added.
			resultMarker?.remove()
			false
		}

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
	private fun initializeMap() {
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync {
			Log.d(TAG, "The map is ready!")
			mMap = it
			mapIsReady = true
			startTrackingTheUser()
		}
	}


	/**
	 * Get the location of the user with the specified id, and show it on the map.
	 */
	private fun showUserLocation(id: String, locationPresenter: LocationPresenter){
		CoroutineScope(Dispatchers.IO).launch {
			val serverConnection = getServerConnection()
			try {
				val result = serverConnection.getUserLocation(id)
				Log.d("test", "result: $result")

				withContext(Dispatchers.Main){
					locationPresenter.show(result.lat, result.lon)
				}

			} catch (e: Exception) {
				// Handle exceptions, like network errors or JSON parsing errors
				Log.d("test", "in the exception: $e")
			}
		}
	}


	interface LocationPresenter{

		fun show(lat: Double, lon: Double)

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
	 * Add the observers to the view model.
	 */
	private fun addLocationObservers() {
		// Observer for location updates.
		viewModel.userLocation.observe(this) { userLocation -> updateUi(userLocation) }

		// One time observer for the animation of the camera.
		viewModel.userLocation.observe(this, object : Observer<Location> {
			override fun onChanged(value: Location) {
				animateCamera(value.latitude, value.longitude)
				viewModel.userLocation.removeObserver(this)
			}
		})
	}


	/**
	 * Update the ui with the given new location.
	 */
	private fun updateUi(userLocation: Location) {
		if (!mapIsReady)
			return

		Log.d(TAG, "update the ui with a new location")

		// Removes the last user location from the map.
		currentLocationMarker?.remove()

		// Add a new marker for the user location.
		currentLocationMarker = addMarkerToMap(userLocation.latitude, userLocation.longitude)
	}


	/**
	 * Add a marker to the map at the specified location.
	 */
	private fun addMarkerToMap(lat: Double, lon: Double) : Marker?{
		// Add marker in the user's location.
		val userLocationCoordinates = LatLng(lat, lon)
		return mMap?.addMarker(MarkerOptions()
			.position(userLocationCoordinates)
			.icon(bitmapDescriptorFromVector(this@IntroActivity, R.drawable.baseline_my_location_24)))

	}


	/**
	 * Animate the camera (changes the zoom from a low zoom to a high).
	 */
	private fun animateCamera(lat: Double, lon: Double) {
		if (!mapIsReady)
			return

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





	companion object {
		val TAG: String
			get() = "MapActivity"
	}


}