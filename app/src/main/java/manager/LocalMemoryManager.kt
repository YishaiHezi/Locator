package manager

import android.content.Context

/**
 * Used to save things to local memory.
 * Currently able to save the user's uid and firebase token to the shared preferences.
 *
 * @author Yishai Hezi
 */
object LocalMemoryManager {


	/**
	 * The shared preferences file name for things related to user identification.
	 */
	private const val PREF_NAME = "user_identification"


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
	private fun save(context: Context, key: String, value: String?) {
		val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		val editor = pref.edit()

		editor.putString(key, value)
		editor.apply()
	}


	/**
	 * Get the given value in the given key.
	 */
	private fun get(context: Context, key: String): String? {
		val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

		return pref.getString(key, null)
	}


	/**
	 * Clears the given value from the given key.
	 */
	fun clear(context: Context, key: String) {
		val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		val editor = pref.edit()

		editor.remove(key)
		editor.apply()
	}


	/**
	 * Get the saved UID.
	 */
	fun getUID(context: Context): String?{
		return get(context, KEY_UID)
	}


	/**
	 * Save the given UID.
	 */
	fun saveUID(context: Context, uid: String?){
		save(context, KEY_UID, uid)
	}


	/**
	 * Get the saved firebase token.
	 */
	fun getFirebaseToken(context: Context): String?{
		return get(context, KEY_FIREBASE_TOKEN)
	}

	/**
	 * Save the given firebase token.
	 */
	fun saveFirebaseToken(context: Context, token: String){
		save(context, KEY_FIREBASE_TOKEN, token)
	}


}