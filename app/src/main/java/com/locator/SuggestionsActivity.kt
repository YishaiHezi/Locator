package com.locator

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.lightme.locator.R

/**
 * In this activity the user can search for the locations of other users.
 *
 * @author Yishai Hezi
 */
class SuggestionsActivity : AppCompatActivity(){


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


	}
}