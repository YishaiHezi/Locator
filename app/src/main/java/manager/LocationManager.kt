package manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.locator.UserLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 *
 * @author Yishai Hezi
 */
object LocationManager {


	private const val TAG = "LocationManager"


	/**
	 * Request the required permissions from the user.
	 */
	fun requestPermissionsIfNeeded(activity: Activity) {
		if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			var array = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				array += Manifest.permission.ACCESS_BACKGROUND_LOCATION
			}

			ActivityCompat.requestPermissions(activity, array, 1)

			return
		}
	}


	/**
	 * Get the current user's location.
	 */
	fun getUserLocation(context: Context): Task<Location>? {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
			ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
			|| ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
			|| ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			Log.e(TAG, "Can't get the user's location! the user needs to approve the location permissions")

			return null
		}

		// Request the current location of the user.
		val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

		return fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)

	}


	/**
	 * Getting the user location as a suspend function
	 */
	suspend fun getUserLocationSuspended(context: Context): UserLocation? {
		return suspendCancellableCoroutine { continuation ->
			val getLocationTask = getUserLocation(context)

			getLocationTask?.let {
				it.addOnSuccessListener { location -> continuation.resumeWith(Result.success(UserLocation(location.latitude, location.longitude))) }.
				addOnFailureListener { exception -> continuation.resumeWithException(exception) }
			} ?: continuation.resume(null)


		}
	}


}