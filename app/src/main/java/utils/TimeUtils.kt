package utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * Methods related to time, date etc.
 *
 * @author Yishai Hezi
 */
class TimeUtils {


	companion object {

		/**
		 * Converts the given time in milliseconds to a string that looks like this: HH:mm, yyyy/MM/dd
		 */
		fun convertToDate(timeMillis: Long): String {
			val formatter = SimpleDateFormat("HH:mm, yyyy/MM/dd", Locale.getDefault())
			val calendar: Calendar = Calendar.getInstance()
			calendar.setTimeInMillis(timeMillis)
			return formatter.format(calendar.time)
		}
	}


}