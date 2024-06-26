package com.locator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lightme.locator.R
import data.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import manager.LocalMemoryManager
import manager.RecentSearchesManager
import request.RequestHandler
import uiutils.LocatorBottomSheetDialog


/**
 * In this activity the user can search for the locations of other users.
 *
 * @author Yishai Hezi
 */
class HomeActivity : AppCompatActivity(R.layout.home_activity), OnUserClickedListener {


	/**
	 * The search job. Should be canceled when another search started.
	 */
	private var searchJob: Job? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val adapter = initAdapter()
		initRecyclerView(adapter)
		initSearchView(adapter)

		logData()
	}


	/**
	 * Initialize the recyclerView.
	 */
	private fun initRecyclerView(adapter: UserAdapter) {
		val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
		recyclerView.setLayoutManager(LinearLayoutManager(this))
		recyclerView.setAdapter(adapter)
		addDividers(recyclerView)
	}


	/**
	 * Initialize the adapter.
	 */
	private fun initAdapter(): UserAdapter {
		val userList: List<UserDetails> = RecentSearchesManager.getRecentSearches(this)
		return UserAdapter(userList, this, supportFragmentManager)
	}


	/**
	 * Initialize the SearchView.
	 */
	private fun initSearchView(adapter: UserAdapter) {
		val searchView: SearchView = findViewById(R.id.search_view)

		val listener = object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				// Perform here the final search when the user clicks on the search button, if needed.
				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				searchJob?.cancel()

				// Perform the live search as the user types
				if (newText.length >= 2) { // This is to prevent too many requests, adjust the number as needed
					performSearch(newText, adapter)
				} else {
					showRecentUsersSearches(adapter)
				}

				return true
			}
		}

		searchView.setOnQueryTextListener(listener)
	}


	/**
	 * Add dividers between elements to the recyclerView.
	 */
	private fun addDividers(recyclerView: RecyclerView?) {
		val context = recyclerView?.context ?: return
		val divider = ContextCompat.getDrawable(context, R.drawable.divider_horizontal)
		val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
			if (divider != null) {
				setDrawable(divider)
			}
		}

		recyclerView.addItemDecoration(dividerItemDecoration)
	}


	/**
	 * Perform the search.
	 */
	private fun performSearch(text: String, adapter: UserAdapter) {
		searchJob = lifecycleScope.launch {
			delay(1000)
			val usersResult = findUsersByText(text)

			usersResult.fold(
				{ usersDetails -> showResults(usersDetails, adapter) },
				{ showResults(emptyList(), adapter) }
			)
		}
	}


	/**
	 * Find the users by prefix. Call the server.
	 */
	private suspend fun findUsersByText(text: String): Result<List<UserDetails>> {
		return withContext(Dispatchers.IO) {
			runCatching {
				val serverConnection = RequestHandler.getServerConnection()
				val users = serverConnection.getUsersByPrefix(text)

				users.map { user -> UserDetails(user.name, user.id) }
			}
		}
	}


	/**
	 * Show the results.
	 */
	private fun showResults(users: List<UserDetails>, adapter: UserAdapter) {
		if (users.isNotEmpty()) {
			adapter.updateUsersList(users)
		} else {
			showRecentUsersSearches(adapter)
		}
	}


	/**
	 * Show the recent users searches.
	 */
	private fun showRecentUsersSearches(adapter: UserAdapter) {
		val recentUsersList: List<UserDetails> = RecentSearchesManager.getRecentSearches(this)
		adapter.updateUsersList(recentUsersList)
	}


	/**
	 * Log the fcm token and the user id for debugging purposes.
	 */
	private fun logData() {
		val fcmToken = LocalMemoryManager.getFirebaseToken(this)
		val uid = LocalMemoryManager.getUID(this)

		Log.d(TAG, "fcmToken: $fcmToken")
		Log.d(TAG, "uid: $uid")

	}


	/**
	 * Implementation for click on a user result.
	 */
	override fun onUserClicked(user: UserDetails) {
		RecentSearchesManager.saveToRecentSearch(this, user)
		startActivity(MapActivity.createStartIntent(this, user.id))

		Log.d(TAG, "Clicked on user: $user")
	}


	/**
	 * The adapter for showing the search results.
	 */
	class UserAdapter(
		private var userList: List<UserDetails>,
		private val userClickedListener: OnUserClickedListener,
		private val fragmentManager: FragmentManager
	) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
			val view: View = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

			return UserViewHolder(view)
		}


		override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
			val user: UserDetails = userList[position]

			val button: Button = holder.itemView.findViewById(R.id.user)
			button.text = user.name
			button.setOnClickListener { userClickedListener.onUserClicked(user) }

			val deleteButton: ImageButton = holder.itemView.findViewById(R.id.delete)
			deleteButton.setOnClickListener { showDeleteConfirmation(it, user) }
		}


		override fun getItemCount(): Int {
			return userList.size
		}


		/**
		 * Update the users list.
		 */
		fun updateUsersList(newUserDetails: List<UserDetails>) {
			userList = newUserDetails
			notifyDataSetChanged()
		}


		/**
		 * Show the delete confirmation dialog.
		 */
		private fun showDeleteConfirmation(view: View, user: UserDetails) {
			val headerText = view.context.getString(R.string.delete_item)
			val buttonText = view.context.getString(R.string.delete)
			val action = createDeleteAction(view, user)

			val bottomSheetDialog = LocatorBottomSheetDialog.newInstance(headerText, buttonText, action)
			bottomSheetDialog.show(fragmentManager, "bottomSheetDialog")
		}


		/**
		 * Create the delete action.
		 */
		private fun createDeleteAction(view: View, user: UserDetails): LocatorBottomSheetDialog.Action {
			return object : LocatorBottomSheetDialog.Action() {
				override fun execute() {
					val users = LocalMemoryManager.getUsers(view.context)
					val filteredUsers = users.filter { currentUser -> currentUser.id != user.id }
					LocalMemoryManager.saveUsers(view.context, filteredUsers)
					updateUsersList(filteredUsers)
				}
			}
		}


		/**
		 * The view holder.
		 */
		class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


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


/**
 * Interface that suppose to be called when a search result (user) is clicked.
 */
interface OnUserClickedListener {
	fun onUserClicked(user: UserDetails)
}

