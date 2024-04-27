package com.locator

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lightme.locator.R
import data.User
import manager.LocalMemoryManager
import manager.RecentSearchesManager


/**
 * In this activity the user can search for the locations of other users.
 *
 * @author Yishai Hezi
 */
class HomeActivity : AppCompatActivity(R.layout.home_activity), OnUserClickedListener {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
		recyclerView.setLayoutManager(LinearLayoutManager(this))

		val userList: List<User> = RecentSearchesManager.getRecentSearches(this)
		val adapter = UserAdapter(userList, this)
		recyclerView.setAdapter(adapter)


		// todo: need to delete this. this is the old implementation of recent searches.
//		setSearchableConfiguration(searchView)
		logData()
	}


	/**
	 * Get the SearchView and set the searchable configuration.
	 */
	private fun setSearchableConfiguration(searchView: SearchView) {
		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, MapActivity::class.java)))
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
	 * Implementation for click on a user.
	 */
	override fun onUserClicked(user: User) {
		RecentSearchesManager.saveToRecentSearch(this, user)
		startActivity(MapActivity.createStartIntent(this, user.id))
	}


	/**
	 * The adapter
	 */
	class UserAdapter(private val userList: List<User>, private val userClickedListener: OnUserClickedListener) :
		RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
			val view: View = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

			return UserViewHolder(view)
		}


		override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
			val user: User = userList[position]
			val textView: TextView = holder.itemView.findViewById(R.id.user)

			textView.text = user.name
			textView.setOnClickListener { userClickedListener.onUserClicked(user) }
		}


		override fun getItemCount(): Int {
			return userList.size
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
 * Interface that suppose to be called when a user is clicked.
 */
interface OnUserClickedListener {
	fun onUserClicked(user: User)
}

