package com.example.swanseahistoryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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
