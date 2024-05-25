package uitls

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lightme.locator.R
import kotlinx.parcelize.Parcelize


/**
 * A general bottom sheet dialog that displays a header and a button.
 *
 * @author Yishai Hezi
 */
class LocatorBottomSheetDialog : BottomSheetDialogFragment() {


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.locator_bottom_sheet_dialog, container, false)
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initHeader(view)
		initButton(view)
	}


	/**
	 * Initializes the header text.
	 */
	private fun initHeader(view: View){
		val headerText = arguments?.getString("headerText")
		view.findViewById<TextView>(R.id.header).text = headerText
	}


	/**
	 * Initializes the button.
	 */
	private fun initButton(view: View){
		val buttonText = arguments?.getString("buttonText")
		val action = arguments?.getParcelable<Action>("action")
		val button = view.findViewById<Button>(R.id.button)

		button.text = buttonText
		button.setOnClickListener {
			action?.execute()
			dismiss()
		}
	}


	/**
	 * Action that should be executed when the button is clicked.
	 */
	@Parcelize
	open class Action: Parcelable{
		open fun execute(){}

	}


	companion object{


		/**
		 * Creates a new instance of LocatorBottomSheetDialog.
		 */
		fun newInstance(headerText: String?, buttonText: String?, action: Action): LocatorBottomSheetDialog {
			return LocatorBottomSheetDialog().apply {
				arguments = Bundle().apply {
					putString("headerText", headerText)
					putString("buttonText", buttonText)
					putParcelable("action", action)
				}
			}
		}


	}


}