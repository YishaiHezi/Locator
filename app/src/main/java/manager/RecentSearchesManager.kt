package manager

import android.content.Context
import data.UserDetails

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
	fun getRecentSearches(context: Context): List<UserDetails>{
		return LocalMemoryManager.getUsers(context)
	}


	/**
	 * Save the given user to the recent searches.
	 */
	fun saveToRecentSearch(context: Context, userDetails: UserDetails){
		val recentSearches = LocalMemoryManager.getUsers(context).toMutableList()

		if (userDetails in recentSearches) {
			recentSearches.remove(userDetails)
		}

		recentSearches.add(0, userDetails)

		if (recentSearches.size > MAX_SIZE_OF_RECENT_SEARCHES) {
			recentSearches.removeAt(MAX_SIZE_OF_RECENT_SEARCHES)
		}

		LocalMemoryManager.saveUsers(context, recentSearches)
	}


}