package com.example.locator

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

	private lateinit var mMap: GoogleMap
//	private lateinit var binding: ActivityMapsBinding
	private var locationManager: LocationManager? = null


	private var isUserCurrentLocationShown: Boolean = false
	private var mapIsReady: Boolean = false



	private lateinit var fusedLocationClient: FusedLocationProviderClient


	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (grantResults.any { it == PackageManager.PERMISSION_GRANTED } && !isUserCurrentLocationShown && mapIsReady)
			addUserCurrentLocationToTheMap()
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)



		// Check for permission
		if (!areLocationPermissionsSet()) {
			ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
		}




		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



		locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?


//		if (areLocationPermissionsSet()) {
//			try {
//				CoroutineScope(Dispatchers.Main).launch {
//					locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this@MapsActivity)
//				}
//			} catch (e : SecurityException){
//				Toast.makeText(this, "something went wrong!", Toast.LENGTH_SHORT).show()
//			}
//
//		}







//		binding = ActivityMapsBinding.inflate(layoutInflater)
		setContentView(R.layout.activity_maps)

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager
			.findFragmentById(R.id.map) as SupportMapFragment
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
		mapIsReady = true
		mMap = googleMap

		// Add a marker in Sydney and move the camera
//		val sydney = LatLng(-34.0, 151.0)
//		mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


		if (areLocationPermissionsSet() && !isUserCurrentLocationShown)
			addUserCurrentLocationToTheMap()
	}


	private fun addUserCurrentLocationToTheMap() {
		if (areLocationPermissionsSet()) {
			try {
				fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
					val lat = location?.latitude
					val lon = location?.longitude

					if (lat != null && lon != null){
						val myLocation = LatLng(lat, lon)
						mMap.addMarker(MarkerOptions().position(myLocation).title("This is your location!"))
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 11.5f))
						isUserCurrentLocationShown = true
					}
					else{
						Toast.makeText(this, "we couldn't find your location!", Toast.LENGTH_SHORT).show()
					}
				}
			}catch (e : SecurityException){
				Toast.makeText(this, "something went wrong!", Toast.LENGTH_SHORT).show()
			}
		}
	}



	override fun onLocationChanged(location: Location) {
		// Get the latitude and longitude
		// Get the latitude and longitude
		val latitude = location.latitude
		val longitude = location.longitude

//		val myLocation = LatLng(latitude, longitude)
//		mMap.addMarker(MarkerOptions().position(myLocation).title("This is your location!"))
//		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 11.5f))
//		locationManager?.removeUpdates(this)
	}
}