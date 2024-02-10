package firebase

import android.location.Location
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.locator.UserLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import manager.LocalMemoryManager
import manager.LocationManager.getUserLocation
import request.RequestHandler


/**
 * Here we have 2 useful methods related to the process of receiving messages from the server
 * using firebase.
 */
class FirebaseLocationsMessagesService : FirebaseMessagingService() {


	/**
	 * Job for sending stuff to the server.
	 */
	private val job = SupervisorJob()


	/**
	 * Scope for sending stuff to the server.
	 */
	private val scope = CoroutineScope(Dispatchers.IO + job)


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
	 * Runs when the app installed, and sometimes after that, when a new token is generated.
	 */
	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Log.d(TAG, "The Firebase token was updated to: $token");

		LocalMemoryManager.saveFirebaseToken(this, token)
		sendTokenToServer(token)
	}


	override fun onDestroy() {
		super.onDestroy()
		job.cancel()
	}


	/**
	 * Send the given token to the server.
	 */
	private fun sendTokenToServer(token: String){
		val uid = LocalMemoryManager.getUID(this) ?: return

		scope.launch {
			val serverConnection = RequestHandler.getServerConnection()
			serverConnection.updateFirebaseToken(uid, token)
		}

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
		getUserLocation(this)?.addOnSuccessListener {
			if (it == null) return@addOnSuccessListener

			Log.d(TAG, "Got a new location of the user: ${it.latitude}, ${it.longitude}")
			sendLocationToServer(it)
		}
	}


	/**
	 * Send the given location to the server (location of the user).
	 */
	private fun sendLocationToServer(location: Location){
		CoroutineScope(Dispatchers.IO).launch {
			val uid = LocalMemoryManager.getUID(this@FirebaseLocationsMessagesService) ?: return@launch

			val serverConnection = RequestHandler.getServerConnection()
			serverConnection.updateUserLocation(uid, UserLocation(location.latitude, location.longitude))
		}
	}


	companion object{


		const val TAG = "test_fcm_messages"


	}


}