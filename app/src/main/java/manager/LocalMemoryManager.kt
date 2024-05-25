package manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.UserDetails


/**
 * Used to save things to local memory.
 * Currently able to save the user's uid and firebase token to the shared preferences.
 *
 * @author Yishai Hezi
 */
object LocalMemoryManager {


	/**
	 * Gson object to convert elements to strings in order to save them.
	 */
	private val gson: Gson = Gson()


	/**
	 * The shared preferences file name for things related to user identification.
	 */
	private const val IDENTIFICATION_PREF_NAME = "user_identification"


	/**
	 * The shared preferences file name for things related to recent searches.
	 */
	private const val RECENT_SEARCHES_PREF_NAME = "recent_searches_pref"


	/**
	 * The key for saving recent searches in the shared preferences.
	 */
	private const val KEY_RECENT_SEARCHES = "recent_searches"


	/**
	 * The user's uid key in the shared preferences.
	 */
	private const val KEY_UID = "uid"


	/**
	 * The user's firebase token key in the shared preferences.
	 */
	private const val KEY_FIREBASE_TOKEN = "firebase_token"


	/**
	 * Save the given value in the given key.
	 */
	private fun save(context: Context, prefName: String, key: String, value: String?) {
		val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
		val editor = pref.edit()

		editor.putString(key, value)
		editor.apply()
	}


	/**
	 * Get the given value in the given key.
	 */
	private fun get(context: Context, prefName: String, key: String): String? {
		val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

		return pref.getString(key, null)
	}


	/**
	 * Clears the given value from the given key.
	 */
	fun clear(context: Context, prefName: String, key: String) {
		val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
		val editor = pref.edit()

		editor.remove(key)
		editor.apply()
	}


	/**
	 * Get a list of users (recent searches).
	 */
	fun getUsers(context: Context): List<UserDetails> {
		val userListJson: String? = get(context, RECENT_SEARCHES_PREF_NAME, KEY_RECENT_SEARCHES)

		val users = if (userListJson != null) {
			val typeToken = object : TypeToken<List<UserDetails>>() {}
			gson.fromJson(userListJson, typeToken)
		} else {
			emptyList()
		}

		return users
	}


	/**
	 * Saves a list of users (recent searches).
	 */
	fun saveUsers(context: Context, userDetails: List<UserDetails>) {
		val type = object : TypeToken<List<UserDetails>>() {}.type
		val usersListJson: String = gson.toJson(userDetails, type)

		save(context, RECENT_SEARCHES_PREF_NAME, KEY_RECENT_SEARCHES, usersListJson)
	}


	/**
	 * Get the saved UID.
	 */
	fun getUID(context: Context): String?{
		return get(context, IDENTIFICATION_PREF_NAME, KEY_UID)
	}


	/**
	 * Save the given UID.
	 */
	fun saveUID(context: Context, uid: String?){
		save(context, IDENTIFICATION_PREF_NAME, KEY_UID, uid)
	}


	/**
	 * Get the saved firebase token.
	 */
	fun getFirebaseToken(context: Context): String?{
		return get(context, IDENTIFICATION_PREF_NAME, KEY_FIREBASE_TOKEN)
	}

	/**
	 * Save the given firebase token.
	 */
	fun saveFirebaseToken(context: Context, token: String){
		save(context, IDENTIFICATION_PREF_NAME, KEY_FIREBASE_TOKEN, token)
	}


}