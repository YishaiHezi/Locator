package request

import com.locator.UserLocation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Here we define the apis that we need to call from the server.
 */
interface ServerConnection {
	@GET("GetUser/{id}")
	suspend fun getUser(@Path("id") userId: String): User


	@GET("GetUserLocation/{id}")
	suspend fun getUserLocation(@Path("id") userId: String): UserLocation


	@POST("AddUser")
	suspend fun addUser(@Body user: User)


	@POST("UpdateUserLocation/{id}")
	suspend fun updateUserLocation(@Path("id") userId: String, @Body location: UserLocation): User

	@POST("UpdateFirebaseToken/{id}")
	suspend fun updateFirebaseToken(@Path("id") userId: String, @Query("firebaseToken") firebaseToken: String)


	@GET("Test")
	suspend fun test(): Message

}


data class User(
	val id: String,
	val name: String,
	val lat: Double,
	val lon: Double,
	val fcmToken: String?
)

data class Message(
	val id: String,
	val name: String,
	val lat: Double,
	val lon: Double
)