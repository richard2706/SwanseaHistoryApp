package com.example.swanseahistoryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Callback
import java.lang.Exception

class PoiDetails : AppCompatActivity(), TextToSpeech.OnInitListener,
    RatingBar.OnRatingBarChangeListener {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_VISITED_ARRAY = "visited_pois"

        private const val POI_COLLECTION = "points_of_interest"
        private const val POI_RATINGS_COLLECTION = "ratings"
        private const val POI_USER_RATING = "rating"
    }

    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser
    private var userType = UserType.GUEST

    private var storageRef = Firebase.storage.reference
    private val db = Firebase.firestore
    private lateinit var textToSpeechService : TextToSpeech
    private lateinit var speakDescriptionButton : Button
    private lateinit var visitedTextView : TextView
    private lateinit var descriptionTextView : TextView
    private lateinit var ratingTextView : TextView

    private var poi : PointOfInterest? = null
    private var visited : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_details)
        setSupportActionBar(findViewById(R.id.poi_details_toolbar))
        getPoiData()
        updateUserType()
        findVisitedState()

        speakDescriptionButton = findViewById(R.id.button_speak_description)
        textToSpeechService = TextToSpeech(this, this)
        visitedTextView = findViewById(R.id.visited_text)
        descriptionTextView = findViewById(R.id.description_text)
        ratingTextView = findViewById(R.id.overall_rating_text)
        displayPoiInfo()
    }

    /**
     * Check the type of the user and update the options menu accordingly
     */
    private fun updateUserType() {
        currentUser = auth.currentUser

        val extraData = intent.extras
        val prevUserType = extraData?.get("userType")
        if (prevUserType != null) {
            userType = prevUserType as UserType
        }
        invalidateOptionsMenu()
    }

    /**
     * Force options menu to update and ratings bar depending on user account level.
     */
    override fun onResume() {
        updateUserType()

        // Display user's individual rating
        if (userType != UserType.GUEST) { // If user is logged in
            findViewById<LinearLayout>(R.id.user_rating_layout).visibility = View.VISIBLE
            val ratingBar = findViewById<RatingBar>(R.id.poi_rating_bar)
            ratingBar.onRatingBarChangeListener = this

            db.collection(POI_COLLECTION).document(poi!!.id).collection(POI_RATINGS_COLLECTION)
                .document(currentUser!!.uid).get()
                .addOnSuccessListener { result ->
                    ratingBar.rating = result.getDouble(POI_USER_RATING)?.toFloat() ?: 0F
                }
        }

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

        if (poi?.address != null) {
            val addressText = findViewById<TextView>(R.id.address_text)
            addressText.text = poi!!.address
            addressText.visibility = View.VISIBLE
        }

        if (poi?.description != null) {
            speakDescriptionButton.visibility = View.VISIBLE
            descriptionTextView.text = poi?.description
        }

        updateOverallRating()
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
        if (userType == UserType.GUEST) return
        Log.i("ratings-debug", "findVisitedState")

        visited = false
        val currentUserUid = currentUser!!.uid
        db.collection(USERS_COLLECTION).document(currentUserUid).get()
            .addOnSuccessListener { result ->
                val visitedPoisArray = result.get(USER_VISITED_ARRAY)
                if (visitedPoisArray != null) {
                    val visitedPoiIds = result.get(USER_VISITED_ARRAY) as ArrayList<String>
                    visited = visitedPoiIds.contains(poi!!.id)
                }

                if (visited == true) {
                    visitedTextView.visibility = View.VISIBLE
                }
                invalidateOptionsMenu() // show the visited menu option
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.error_visited_state_not_determined))
            }
    }

    /**
     * Show menu options for the correct user type.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_poi_details, menu)

        menu?.findItem(R.id.action_mark_visited)?.isVisible =
            (userType == UserType.STANDARD || userType == UserType.ADMIN) && visited != null
        menu?.findItem(R.id.action_edit)?.isVisible = userType == UserType.ADMIN

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handle actions when a menu option is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_mark_visited -> toggleUserVisitedState()
            R.id.action_edit -> navigateToEditPoi()
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
     * Updates the users rating of the PoI when the ratings bar rating changes.
     */
    override fun onRatingChanged(ratingBar : RatingBar?, rating : Float, fromUser : Boolean) {
        if (userType == UserType.GUEST) return

        val data = hashMapOf("rating" to rating)
        db.collection(POI_COLLECTION).document(poi!!.id).collection(POI_RATINGS_COLLECTION)
            .document(currentUser!!.uid).set(data)
            .addOnSuccessListener {
                displayMessage(getString(R.string.message_rating_updated))
                updateOverallRating()
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.error_rating_not_updated))
            }
    }

    /**
     * Recalculates and displays the overall rating.
     */
    private fun updateOverallRating() {
        db.collection(POI_COLLECTION).document(poi!!.id).collection(POI_RATINGS_COLLECTION).get()
            .addOnSuccessListener { result ->
                val numRatings = result.size()
                if (numRatings > 0) {
                    var totalRating = 0.0
                    for (entry in result.documents)
                        totalRating += entry.getDouble(POI_USER_RATING) ?: 0.0
                    val overallRating = totalRating / numRatings
                    ratingTextView.text = getString(R.string.message_overall_rating, overallRating)
                }
            }
    }

    /**
     * Updates the user's visited state for this PoI.
     */
    private fun toggleUserVisitedState() {
        if (visited == null) return

        val currentUserUid = currentUser!!.uid
        val currentUserDocument = db.collection(USERS_COLLECTION).document(currentUserUid)

        if (visited == false)
            currentUserDocument.update(USER_VISITED_ARRAY, FieldValue.arrayUnion(poi?.id))
            .addOnSuccessListener {
                visited = true
                visitedTextView.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.error_visited_state_not_updated))
            }

        if (visited == true)
            currentUserDocument.update(USER_VISITED_ARRAY, FieldValue.arrayRemove(poi?.id))
            .addOnSuccessListener {
                visited = false
                visitedTextView.visibility = View.GONE
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.error_visited_state_not_updated))
            }
    }

    /**
     * Navigate to the edit PoI screen.
     */
    private fun navigateToEditPoi() {
        val intent  = Intent(this, EditPoiActivity::class.java)
        intent.putExtras(poi!!.toBundle())
        intent.putExtra("newPoi", false)
        startActivity(intent)
    }

    /**
     * Display a long snackbar message.
     */
    private fun displayMessage(message : String) {
        Snackbar.make(findViewById(R.id.poi_details_root), message, Snackbar.LENGTH_LONG).show()
    }
}
