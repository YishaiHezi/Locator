package com.locator

import android.content.SearchRecentSuggestionsProvider

/**
 * The content provider used to store the recent searches by the user, in order
 * to present them as a suggestion the next time the user perform a search.
 *
 * @author Yishai Hezi
 */


// todo: need to delete this

class SuggestionsContentProvider : SearchRecentSuggestionsProvider() {

	init {
		setupSuggestions(AUTHORITY, MODE)
	}

	companion object {
		const val AUTHORITY = "com.locator.SuggestionsContentProvider"
		const val MODE: Int = DATABASE_MODE_QUERIES
	}
}