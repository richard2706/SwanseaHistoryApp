package com.example.swanseahistoryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.swanseahistoryapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    companion object {
        private val SWANSEA_LOCATION = LatLng(51.6255408,-3.9655064)
        private const val DEFAULT_ZOOM = 11F

        private const val POI_COLLECTION = "points_of_interest"
        private const val POI_NAME_FIELD = "name"
        private const val POI_ADDRESS_FIELD = "address"
        private const val POI_DESCRIPTION_FIELD = "description"
        private const val POI_LOCATION_FIELD = "location"
        private const val POI_IMAGE_URL_FIELD = "image_url"

        private const val USERS_COLLECTION = "users"
        private const val USER_NOTIFICATIONS_FIELD = "nearby_notifications"
    }

    // Map related variables
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var pois: List<PointOfInterest>
    private var dbReady = false
    private var mapReady = false
    private var markersLoaded = false
    private var locationPermissionGranted = false

    private val db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()
    private var currentUser = auth.currentUser
    private var userType : UserType? = null
    private var notificationsEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.home_toolbar))

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadPois()
        checkLocationPermissions()
    }

    /**
     * Display the message, if present, when the user navigates back to the home screen.
     */
    override fun onResume() {
        super.onResume()
        currentUser = auth.currentUser // Update logged in user
        val extraData = intent.extras

        val message = extraData?.getString("message")
        if (message != null) displayMessage(message)
        intent.removeExtra("message")

        val updatedUserType = extraData?.get("userType")
        val updatedNotificationsEnabled = extraData?.get("notificationsEnabled")
        if (currentUser != null) { // if user is logged in
            if (updatedUserType == null) { // account type not specified
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                userType = updatedUserType as UserType
                notificationsEnabled = updatedNotificationsEnabled as Boolean
            }
        } else { // not logged in
            userType = UserType.GUEST
        }
        invalidateOptionsMenu() // Update the menu for the user's account type
    }

    /**
     * Load the POIs from the database
     */
    private fun loadPois() {
        db.collection(POI_COLLECTION).get()
            .addOnSuccessListener { result ->
                parsePois(result)
                dbReady = true
                displayPoiMarkers()
            }
            .addOnFailureListener { exception ->
                displayMessage(getString(R.string.message_pois_not_loaded))
                Log.w("firebase-log", exception)
            }
    }

    /**
     * Stores the data from the database in a list of PointOfInterest objects.
     */
    private fun parsePois(result : QuerySnapshot) {
        var poiList  = mutableListOf<PointOfInterest>()
        for (document in result) {
            val id = document.id
            val name = document.getString(POI_NAME_FIELD)
            val address = document.getString(POI_ADDRESS_FIELD)
            val description = document.getString(POI_DESCRIPTION_FIELD)
            val location = document.getGeoPoint(POI_LOCATION_FIELD)
            val imageURL = document.getString(POI_IMAGE_URL_FIELD)
            poiList.add(PointOfInterest(id, name, address, description, location, imageURL))
        }
        pois = poiList
    }

    /**
     * Checks and requests location permissions to display the user's location on the map.
     */
    private fun checkLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    locationPermissionGranted = true
                    showMyLocation()
                }
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    locationPermissionGranted = true
                    showMyLocation()
                }
            }
        }

        // Request permission if not already granted
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /**
     * Shows the user's current location on the map, if location permissions have been granted.
     */
    @SuppressLint("MissingPermission")
    private fun showMyLocation() {
        if (!locationPermissionGranted || !mapReady) return
        map.isMyLocationEnabled = true
    }

    /**
     * Display the map markers for all PoIs.
     */
    private fun displayPoiMarkers() {
        if (markersLoaded || !mapReady || !dbReady) return
        for (poi in pois) {
            if (poi.location == null) continue
            val poiPosition = LatLng(poi.location.latitude, poi.location.longitude)
            val marker = map.addMarker(MarkerOptions()
                .position(poiPosition)
                .title(poi.name)
            )
            if (marker != null) marker.tag = poi
        }
        markersLoaded = true
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SWANSEA_LOCATION, DEFAULT_ZOOM))
        mapReady = true
        displayPoiMarkers()
        showMyLocation()

        map.setInfoWindowAdapter(PoiMarkerInfoWindowAdapter(this))
        map.setOnInfoWindowClickListener(this)
    }

    /**
     * Populate the toolbar menu with the home screen actions. Menu will be empty until user type
     * has been confirmed.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (userType == null) return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.home_menu, menu)

        val isLoggedIn = userType != UserType.GUEST
        menu?.findItem(R.id.action_login)?.isVisible = !isLoggedIn
        menu?.findItem(R.id.action_logout)?.isVisible = isLoggedIn
        menu?.findItem(R.id.action_email_visited)?.isVisible = isLoggedIn
        menu?.findItem(R.id.action_enable_notifications)?.isVisible =
            isLoggedIn && !notificationsEnabled
        menu?.findItem(R.id.action_disable_notifications)?.isVisible =
            isLoggedIn && notificationsEnabled
        menu?.findItem(R.id.action_add)?.isVisible = userType == UserType.ADMIN

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Controls the actions taken when a menu item is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_login -> startActivity(Intent(this, LoginActivity::class.java))
            R.id.action_logout -> logoutUser()
            R.id.action_enable_notifications -> enableNotifications()
            R.id.action_disable_notifications -> disableNotifications()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Navigate to POI details screen when marker info window is clicked.
     */
    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(this, PoiDetails::class.java)
        val poiBundle = (marker.tag as PointOfInterest).toBundle()
        intent.putExtras(poiBundle)
        intent.putExtra("userType", userType)
        startActivity(intent)
    }

    /**
     * Logs the user out (if logged in) and displays a message.
     */
    private fun logoutUser() {
        if (currentUser == null) return
        auth.signOut()
        currentUser = null // Clear current user
        userType = UserType.GUEST

        invalidateOptionsMenu() // Update menu options
        displayMessage(getString(R.string.message_logout))
    }

    /**
     * Enable notifications when the user is nearby a point of interest.
     */
    private fun enableNotifications() {
        if (notificationsEnabled) {
            displayMessage(getString(R.string.message_notifications_enabled))
            return
        }

        // Update user preference in database
        val currentUserUid = currentUser!!.uid
        val data = hashMapOf(USER_NOTIFICATIONS_FIELD to true)
        db.collection(USERS_COLLECTION).document(currentUserUid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Request background notification permission
                // Start background service
                displayMessage(getString(R.string.message_notifications_enabled))
                notificationsEnabled = true
                invalidateOptionsMenu()
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.message_update_prefs_failed))
            }
    }

    /**
     * Disable notifications when the user is nearby a point of interest.
     */
    private fun disableNotifications() {
        if (!notificationsEnabled) {
            displayMessage(getString(R.string.message_notifications_disabled))
            return
        }

        // Update user preference in database
        val currentUserUid = currentUser!!.uid
        val data = hashMapOf(USER_NOTIFICATIONS_FIELD to false)
        db.collection(USERS_COLLECTION).document(currentUserUid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Stop background service
                displayMessage(getString(R.string.message_notifications_disabled))
                notificationsEnabled = false
                invalidateOptionsMenu()
            }
            .addOnFailureListener {
                displayMessage(getString(R.string.message_update_prefs_failed))
            }
    }

    /**
     * Display a long snackbar message.
     */
    private fun displayMessage(message : String) {
        Snackbar.make(findViewById(R.id.home_root), message, Snackbar.LENGTH_LONG).show()
    }
}
