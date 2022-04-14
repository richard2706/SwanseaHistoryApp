package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Callback
import java.lang.Exception

class PoiDetails : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_VISITED_ARRAY = "visited_pois"
    }

    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser
    private var userType = UserType.GUEST

    private var storageRef = Firebase.storage.reference
    private val db = Firebase.firestore
    private lateinit var textToSpeechService : TextToSpeech
    private lateinit var speakDescriptionButton : Button
    private lateinit var descriptionTextView : TextView

    private var poi : PointOfInterest? = null
    private var visited : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poidetails)
        setSupportActionBar(findViewById(R.id.poi_details_toolbar))
        getPoiData()
        findVisitedState()

        speakDescriptionButton = findViewById(R.id.button_speak_description)
        textToSpeechService = TextToSpeech(this, this)
        descriptionTextView = findViewById(R.id.description_text)
        displayPoiInfo()
    }

    /**
     * Force options menu to update, if
     */
    override fun onResume() {
        currentUser = auth.currentUser

        val extraData = intent.extras
        val prevUserType = extraData?.get("userType")
        if (prevUserType != null) {
            userType = prevUserType as UserType
        }
        invalidateOptionsMenu()

        super.onResume()
    }

    /**
     * Release the text to speech service's resources when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        textToSpeechService.shutdown()
    }

    /**
     * When the text to speech service is initialised, enable the speak description button.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            speakDescriptionButton.isEnabled = true
        } else {
            displayMessage(getString(R.string.error_text_to_speech_error))
        }
    }

    /**
     * Retrieve POI data from the intent.
     */
    private fun getPoiData() {
        val intentData = intent.extras ?: return
        val id = intentData.getString("id") ?: return
        val name = intentData.getString("name")
        val address = intentData.getString("address")
        val description = intentData.getString("description")
        val imageURL = intentData.getString("imageURL")
        val hasLocation = intentData.getBoolean("hasLocation")
        val latitude = intentData.getDouble("latitude")
        val longitude = intentData.getDouble("longitude")
        val location = if (hasLocation) GeoPoint(latitude, longitude) else null

        poi = PointOfInterest(id, name, address, description, location, imageURL)
    }

    /**
     * Display the available information about the POI in the layout.
     */
    private fun displayPoiInfo() {
        if (poi == null) return
        if (poi?.name != null) findViewById<Toolbar>(R.id.poi_details_toolbar).title = poi!!.name
        if (poi?.imageURL != null) displayPoiImage(poi!!.imageURL!!)

        // display visited message if visited

        if (poi?.address != null) {
            val addressText = findViewById<TextView>(R.id.address_text)
            addressText.text = poi!!.address
            addressText.visibility = View.VISIBLE
        }

        if (poi?.description != null) {
            speakDescriptionButton.visibility = View.VISIBLE
            descriptionTextView.text = poi?.description
        }
    }

    /**
     * Displays the image at the given URL on the screen.
     */
    private fun displayPoiImage(imageURL : String) {
        val imageView = findViewById<ImageView>(R.id.poi_image)
        Picasso.get().load(imageURL).into(imageView, object : Callback {
            override fun onSuccess() {}
            override fun onError(exception: Exception) {
                displayMessage(getString(R.string.error_poi_image_not_loaded))
                Log.e("poi-details", exception.toString())
            }
        })
    }

    /**
     * Query the database to find out if the user has visited this PoI.
     */
    private fun findVisitedState() {
        val currentUserUid = currentUser!!.uid
        db.collection(USERS_COLLECTION).document(currentUserUid).get()
            .addOnSuccessListener { result ->
                val visitedPoisArray = result.get(USER_VISITED_ARRAY)
                visited = if (visitedPoisArray == null) false else {
                    val visitedPoiIds = result.get(USER_VISITED_ARRAY) as ArrayList<String>
                    visitedPoiIds.contains(poi!!.id)
                }
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.error_visited_state_not_determined))
            }
    }

    /**
     * Show menu options for the correct user type.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.poi_details_menu, menu)

        val markVisitedAction = menu?.findItem(R.id.action_mark_visited)
        if (markVisitedAction != null) markVisitedAction.isVisible =
            userType == UserType.STANDARD || userType == UserType.ADMIN

        val editAction = menu?.findItem(R.id.action_edit)
        if (editAction != null) editAction.isVisible = userType == UserType.ADMIN

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handle actions when a menu option is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_mark_visited -> toggleUserVisitedState()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Speaks the description text when the speak description button is clicked.
     */
    fun onSpeakDescriptionButtonClick(view : View) {
        val text = descriptionTextView.text.toString()
        textToSpeechService.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    /**
     *
     */
    private fun toggleUserVisitedState() {

    }

    /**
     * Display a long snackbar message.
     */
    private fun displayMessage(message : String) {
        Snackbar.make(findViewById(R.id.poi_details_root), message, Snackbar.LENGTH_LONG).show()
    }
}