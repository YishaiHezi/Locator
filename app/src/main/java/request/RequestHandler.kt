package request

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Handler for all the requests.
 */
object RequestHandler {


	/**
	 * Returns a [ServerConnection] object that is used to send requests to the server.
	 */
	fun getServerConnection() : ServerConnection {
		val retrofit = Retrofit.Builder()
			.baseUrl("https://192.168.1.179:8443")
			.client(getUnsafeOkHttpClient())
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		return retrofit.create(ServerConnection::class.java)
	}


	/**
	 * Returns unsafe http client. This code needs to be removed because it is not safe -
	 * and should be replaced with a code that returns a safe http client.
	 */
	private fun getUnsafeOkHttpClient(): OkHttpClient {
		return try {
			val trustAllCerts = arrayOf<TrustManager>(
				object : X509TrustManager {
					override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
					override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
					override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
				}
			)

			val sslContext = SSLContext.getInstance("SSL")
			sslContext.init(null, trustAllCerts, java.security.SecureRandom())

			val sslSocketFactory = sslContext.socketFactory

			OkHttpClient.Builder()
				.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
				.hostnameVerifier { _, _ -> true }
				.build()
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}


}



