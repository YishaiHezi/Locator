package com.locator

import android.content.DialogInterface
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.lightme.locator.R

/**
 * The general error dialog.
 *
 * @author Yishai Hezi
 */
class ErrorDialog : DialogFragment(R.layout.error_fragment) {


	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)

		parentFragmentManager.setFragmentResult("dialog_dismissed", bundleOf())
	}


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