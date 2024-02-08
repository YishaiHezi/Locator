package com.locator

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.messaging.FirebaseMessaging
import com.lightme.locator.R

/**
 * In this activity the user can search for the locations of other users.
 *
 * @author Yishai Hezi
 */
class SuggestionsActivity : AppCompatActivity() {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.suggestions_activity)


		val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
		setSupportActionBar(myToolbar)


		// Get the SearchView and set the searchable configuration.
		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView: SearchView = findViewById(R.id.search_view)

		searchView.apply {
			isSubmitButtonEnabled = true
			setSearchableInfo(searchManager.getSearchableInfo(ComponentName(context, MapActivity::class.java)))
		}


		// todo: delete this:
		printToken()


	}


	// TODO: this is a test, needs to be deleted

	private fun printToken() {
		FirebaseMessaging.getInstance().token
			.addOnCompleteListener { task ->
				if (!task.isSuccessful) {
					Log.w("test_token", "getInstanceId failed", task.exception)
					return@addOnCompleteListener
				}

				// Get the token
				val token: String = task.result

				// Log the token
				Log.d("test_token", token)
			}
			.addOnFailureListener {
				Log.w("test_token", "in failure listener")
			}
	}


	companion object {


		/**
		 * Creates a starting intent for this activity.
		 */
		fun createStartIntent(context: Context): Intent {

			return Intent(context, SuggestionsActivity::class.java)

		}

	}

}