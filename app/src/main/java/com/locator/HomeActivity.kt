package com.locator

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.lightme.locator.R
import manager.LocalMemoryManager

/**
 * In this activity the user can search for the locations of other users.
 *
 * @author Yishai Hezi
 */
class HomeActivity : AppCompatActivity(R.layout.home_activity) {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setSearchableConfiguration()
		logData()
	}


	/**
	 * Get the SearchView and set the searchable configuration.
	 */
	private fun setSearchableConfiguration(){

		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView: SearchView = findViewById(R.id.search_view)

		searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, MapActivity::class.java)))
	}


	/**
	 * Log the fcm token and the user id for debugging purposes.
	 */
	private fun logData(){
		val fcmToken = LocalMemoryManager.getFirebaseToken(this)
		val uid = LocalMemoryManager.getUID(this)

		Log.d(TAG, "fcmToken: $fcmToken")
		Log.d(TAG, "uid: $uid")

	}


	companion object {


		/**
		 * The TAG for logging.
		 */
		private const val TAG = "HomeActivity"


		/**
		 * Creates a starting intent for this activity.
		 */
		fun createStartIntent(context: Context): Intent {

			return Intent(context, HomeActivity::class.java)

		}

	}

}