package com.example.swanseahistoryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_IS_ADMIN_FIELD = "isAdmin"
    }

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    /**
     * When the user navigates to this login activity, return to the home screen if they are
     * already logged in.
     */
    override fun onStart() {
        super.onStart()
        if (currentUser != null) handleSuccessfulLogin()
    }

    /**
     * Attempts to log the user in with the email and password they provided. Navigates to the home
     * screen if the user logged in successfully.
     */
    fun onLoginButtonClick(view: View) {
        closeKeyboard()
        val email = findViewById<EditText>(R.id.email_text_input).text.toString()
        val password  = findViewById<EditText>(R.id.password_input).text.toString()

        // Display message if email or password is blank
        val emailMissing = email == ""
        val passwordMissing = password == ""
        if (emailMissing || passwordMissing) {
            if (emailMissing && passwordMissing)
                displayMessage(getString(R.string.message_missing_email_password))
            else if (emailMissing)
                displayMessage(getString(R.string.message_missing_email))
            else if (passwordMissing)
                displayMessage(getString(R.string.message_missing_password))
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) handleSuccessfulLogin()
                else handleFailedLogin()
            }
    }

    /**
     * Determines the user's account type then navigates the user back to the home screen and
     * displays a corresponding message.
     */
    private fun handleSuccessfulLogin() {
        currentUser = auth.currentUser
        if (currentUser == null) handleFailedLogin()

        var loginMessage = getString(R.string.message_successful_login, currentUser!!.email)

        // Determine user type
        var userType = UserType.STANDARD
        val currentUserUid = currentUser!!.uid
        db.collection(USERS_COLLECTION).document(currentUserUid).get()
            .addOnSuccessListener { result ->
                val userIsAdmin = result.getBoolean(USER_IS_ADMIN_FIELD)
                if (userIsAdmin == true) userType = UserType.ADMIN
            }
            .addOnFailureListener {
                loginMessage += " " + getString(R.string.message_admin_verification_failed)
            }
            .addOnCompleteListener {
                // Navigate to home screen with message and admin user status
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("message", loginMessage)
                intent.putExtra("userType", userType)
                startActivity(intent)
            }
    }

    /**
     * Notifies the user that they could not be logged in.
     */
    private fun handleFailedLogin() {
        displayMessage(getString(R.string.message_failed_login))
    }

    /**
     * Display a long snackbar message.
     */
    private fun displayMessage(message : String) {
        Snackbar.make(findViewById(R.id.login_root), message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Closes the on screen keyboard.
     */
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
