package com.locator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lightme.locator.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import manager.LocalMemoryManager
import manager.LocationManager
import request.RequestHandler
import request.User


/**
 *
 *
 * @author Yishai Hezi
 */
class LoginActivity : AppCompatActivity(R.layout.login_activity) {


	/**
	 *
	 */
	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Initialize Firebase Auth.
		auth = Firebase.auth

		// Initialize the UI.
		val emailInput: EditText = findViewById(R.id.email_input)
		val passwordInput: EditText = findViewById(R.id.password_input)

		initSignInButton(emailInput, passwordInput)
		initSignUpButton(emailInput, passwordInput)
	}


	public override fun onStart() {
		super.onStart()

		tryToOpenHomeScreen()
	}


	/**
	 * Initialize the sign in button.
	 */
	private fun initSignInButton(emailInput: EditText, passwordInput: EditText) {
		val signInButton: Button = findViewById(R.id.sign_in_button)
		signInButton.setOnClickListener {
			val email = emailInput.text.toString()
			val password = passwordInput.text.toString()

			signIn(email, password)
		}
	}


	/**
	 * Initialize the sign up button.
	 */
	private fun initSignUpButton(emailInput: EditText, passwordInput: EditText) {
		val signUpButton: Button = findViewById(R.id.sign_up_button)
		signUpButton.setOnClickListener {
			val email = emailInput.text.toString()
			val password = passwordInput.text.toString()

			signUp(email, password)
		}
	}


	/**
	 * Here we sign up the user using firebase authentication.
	 */
	private fun signUp(email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(this) { task ->

				if (task.isSuccessful) {
					// Sign in success, update UI with the signed-in user's information
					Log.d(TAG, "createUserWithEmail:success")
					Toast.makeText(baseContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()

					saveUserId(auth.currentUser)

					lifecycleScope.launch {
						sendUserToServer()
						tryToOpenHomeScreen()
					}

				} else {
					// If sign in fails, display a message to the user.
					Log.w(TAG, "createUserWithEmail:failure", task.exception)
					Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
				}
			}
	}


	/**
	 * Here we sign in the user using firebase authentication.
	 */
	private fun signIn(email: String, password: String) {
		auth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this) { task ->

				if (task.isSuccessful) {
					// Sign in success, update UI with the signed-in user's information
					Log.d(TAG, "signInWithEmail:success")
					Toast.makeText(baseContext, "Signing in succeeded!", Toast.LENGTH_SHORT).show()

					saveUserId(auth.currentUser)

					lifecycleScope.launch {
						sendUserToServer()
						tryToOpenHomeScreen()
					}

				} else {
					// If sign in fails, display a message to the user.
					Log.w(TAG, "signInWithEmail:failure", task.exception)
					Toast.makeText(baseContext, "Signing in failed..", Toast.LENGTH_SHORT).show()
				}
			}
	}


	/**
	 * Save the user firebase uid in the local memory.
	 */
	private fun saveUserId(firebaseUser: FirebaseUser?){
		if (firebaseUser == null) return

		LocalMemoryManager.saveUID(this,firebaseUser.uid)
	}


	/**
	 * Sends the user's details to the server.
	 */
	private suspend fun sendUserToServer(){
		withContext(Dispatchers.IO) {
			val uid = LocalMemoryManager.getUID(this@LoginActivity) ?: return@withContext
			val name = "random name" // todo: fix this!
			val location = LocationManager.getUserLocationSuspended(this@LoginActivity)
			val lat: Double = location?.lat ?: 0.0
			val lon = location?.lon ?: 0.0
			val firebaseToken = LocalMemoryManager.getFirebaseToken(this@LoginActivity)
			val user = User(uid, name, lat, lon, firebaseToken)

			val serverConnection = RequestHandler.getServerConnection()
			serverConnection.addUser(user)
		}
	}


	/**
	 * Opens the [SuggestionsActivity] if the user is logged in.
	 */
	private fun tryToOpenHomeScreen() {
		// if currentUser != null -> the user is signed in. If not, we will not open the home screen.
		auth.currentUser ?: return

		startActivity(SuggestionsActivity.createStartIntent(this))
		finish()
	}


	// todo: inside "auth.currentUser" there is "getIdToken". this method returns a unique token for the client, so
	// todo: I should use this in the server to uniquely identify the user on the server (there is 1 more step to do in the server).

	// todo: Need to run the firebase emulator in order to test login flows: https://firebase.google.com/docs/auth/android/start

	// todo: I have a user: yishaihazi@gmail.com, password: 123456
	// todo: I have a user: yishaihazianother@gmail.com, password: 12345678


	companion object {


		private const val TAG = "LoginActivity"


	}


}