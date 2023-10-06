package request

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


/**
 * Here we define the apis that we need to call from the server.
 */
interface ApiService {
	@GET("User/{id}")
	suspend fun getUser(@Path("id") userId: String): User


	@POST("User")
	suspend fun addUser(@Body user: User): User


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