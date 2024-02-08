package com.locator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lightme.locator.R


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

		updateUi()
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

					updateUi()
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

					updateUi()
				} else {
					// If sign in fails, display a message to the user.
					Log.w(TAG, "signInWithEmail:failure", task.exception)
					Toast.makeText(baseContext, "Signing in failed..", Toast.LENGTH_SHORT).show()
				}
			}
	}


	/**
	 * Opens the [SuggestionsActivity] if the user is logged in.
	 */
	private fun updateUi() {
		val currentUser = auth.currentUser

		// Check if user is signed in (non-null) and update UI accordingly.
		if (currentUser != null) {
			startActivity(SuggestionsActivity.createStartIntent(this))
			finish()
		}

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