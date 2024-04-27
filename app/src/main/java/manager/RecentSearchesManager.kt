package manager

import android.content.Context
import data.User

/**
 * Holds the logic for the saving of recent searches.
 *
 * @author Yishai Hezi
 */
object RecentSearchesManager {


	/**
	 * The maximum length of the recent searches.
	 */
	private const val MAX_SIZE_OF_RECENT_SEARCHES = 10


	/**
	 * Get a list of the recent searches.
	 */
	fun getRecentSearches(context: Context): List<User>{
		return LocalMemoryManager.getUsers(context)
	}


	/**
	 * Save the given user to the recent searches.
	 */
	fun saveToRecentSearch(context: Context, user: User){
		val recentSearches = LocalMemoryManager.getUsers(context).toMutableList()

		if (user in recentSearches) {
			recentSearches.remove(user)
		}

		recentSearches.add(0, user)

		if (recentSearches.size > MAX_SIZE_OF_RECENT_SEARCHES) {
			recentSearches.removeAt(MAX_SIZE_OF_RECENT_SEARCHES)
		}

		LocalMemoryManager.saveUsers(context, recentSearches)
	}


}