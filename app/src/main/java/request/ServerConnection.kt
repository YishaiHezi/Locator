package request

import android.location.Location
import com.locator.UserLocation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


/**
 * Here we define the apis that we need to call from the server.
 */
interface ServerConnection {
	@GET("GetUser/{id}")
	suspend fun getUserLocation(@Path("id") userId: String): User


	@POST("AddUser")
	suspend fun addUser(@Body user: User): User


	@POST("UpdateUserLocation/{id}")
	suspend fun updateUserLocation(@Path("id") userId: String, @Body location: UserLocation): User


	@GET("Test")
	suspend fun test(): Message

}


data class User(
	val id: String,
	val name: String,
	val lat: Double,
	val lon: Double
)

data class Message(
	val id: String,
	val name: String,
	val lat: Double,
	val lon: Double
)