package firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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

		// Check if the message contains data payload
		if (message.data.isNotEmpty()) {
			// Handle the data message
			// For simplicity, just log it
			Log.d("test_fcm_message", "Message data payload: " + message.data);
		}

		// Check if the message contains a notification payload
		if (message.notification != null) {
			// For simplicity, just log it
			Log.d("test_fcm_message", "Message Notification Body: " + message.notification?.body);
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
		Log.d("test_fcm_message", "Refreshed token: $token");
	}


}