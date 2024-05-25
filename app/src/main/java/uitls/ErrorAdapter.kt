package uitls

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lightme.locator.R


/**
 * The error adapter. used to show error message in a recyclerView.
 *
 * @author Yishai Hezi
 */
class ErrorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.error_item, parent, false)

		return ErrorViewHolder(view)
	}


	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}


	override fun getItemCount(): Int {
		return 1
	}


	/**
	 * The view holder.
	 */
	class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}