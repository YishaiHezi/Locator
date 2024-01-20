package firebase

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.locator.UserLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import request.RequestHandler


/**
 * Here we have 2 useful methods related to the process of receiving messages from the server
 * using firebase.
 */
class FirebaseLocationsMessagesService : FirebaseMessagingService() {


	/**
	 * Here we handle messages received from the server (using firebase).
	 */
	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)

		Log.d(TAG, "message received!")

		// Check if the message contains data payload
		if (message.data.isNotEmpty()) {
			// Handle the data message
			onCommandReceived(message.data)
		}

		// Check if the message contains a notification payload
		if (message.notification != null) {
			// For simplicity, just log it
			Log.d(TAG, "Message Notification Body: " + message.notification?.body);
		}
	}


	/**
	 * This method runs when the client is granted with a new token from firebase.
	 * Usually runs only when the app installed.
	 */
	override fun onNewToken(token: String) {
		super.onNewToken(token);
		// This method is called when a new token is generated.
		// You should send this token to your server to keep track of user devices.
		Log.d(TAG, "Refreshed token: $token");
	}


	/**
	 * Do what the commands says.
	 */
	private fun onCommandReceived(commands: Map<String, String>){
		if (commands["title"].equals("update_location")) {
			updateUserLocation()
		}
	}


	/**
	 * Get the current user's location, and send it to the server.
	 */
	private fun updateUserLocation(){
		val locationTask = getUserLocation()

		locationTask?.addOnSuccessListener {

			if (it == null) {
				Log.e(TAG, "The location is null!")
				return@addOnSuccessListener
			}

			Log.d(TAG, "the location: ${it.latitude}, ${it.longitude}");

			sendLocationToServer(it)

		}
	}


	/**
	 * Get the current user's location.
	 */
	private fun getUserLocation(): Task<Location>?{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
			ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
			|| ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
			|| ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

			Log.e(TAG, "Can't get the user's location! the user needs to approve the location permissions")

			return null
		}

		// Request the current location of the user.
		val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		return fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)

	}


	/**
	 * Send the given location to the server - as the location of the user.
	 */
	private fun sendLocationToServer(location: Location){
		CoroutineScope(Dispatchers.IO).launch {
			val serverConnection = RequestHandler.getServerConnection()

			// todo: Need to create a userId to every user.
			serverConnection.updateUserLocation("1112", UserLocation(location.latitude, location.longitude))
		}
	}


	companion object{

		const val TAG = "test_fcm_messages"

	}


}