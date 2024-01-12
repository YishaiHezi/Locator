package com.locator

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.lightme.locator.R

/**
 * The general error dialog.
 *
 * @author Yishai Hezi
 */
class ErrorDialog : DialogFragment(R.layout.error_fragment) {


	companion object {


		/**
		 * The tag for the error fragment.
		 */
		private const val TAG = "ErrorDialog"


		fun show(fragmentManager: FragmentManager) {
			ErrorDialog().show(fragmentManager, TAG)
		}


	}


}