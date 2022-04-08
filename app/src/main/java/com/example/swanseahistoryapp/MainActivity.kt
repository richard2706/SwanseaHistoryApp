package com.example.swanseahistoryapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.swanseahistoryapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val SWANSEA_LOCATION = LatLng(51.6255408,-3.9655064)
        private const val DEFAULT_ZOOM = 11F
        private const val POI_COLLECTION_NAME = "points_of_interest"
        private const val NAME_FIELD = "name"
        private const val ADDRESS_FIELD = "address"
        private const val DESCRIPTION_FIELD = "description"
        private const val LOCATION_FIELD = "location"
        private const val IMAGE_URL_FIELD = "image_url"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var pois: List<PointOfInterest>
    private val db = Firebase.firestore
    private var dbReady = false
    private var mapReady = false
    private var markersLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db.collection(POI_COLLECTION_NAME).get()
            .addOnSuccessListener { result ->
                parsePois(result)
                dbReady = true
                displayPoiMarkers()
            }
            .addOnFailureListener { exception -> Log.w("firebase-log", exception) }
    }

    /**
     * Stores the data from the database in a list of PointOfInterest objects.
     */
    private fun parsePois(result : QuerySnapshot) {
        var poiList  = mutableListOf<PointOfInterest>()
        for (document in result) {
            val id = document.id
            val name = document.getString(NAME_FIELD)
            val address = document.getString(ADDRESS_FIELD)
            val description = document.getString(DESCRIPTION_FIELD)
            val location = document.getGeoPoint(LOCATION_FIELD)
            val imageURL = document.getString(IMAGE_URL_FIELD)
            poiList.add(PointOfInterest(id, name, address, description, location, imageURL))
        }
        pois = poiList
    }

    /**
     * Display the map markers for all PoIs.
     */
    private fun displayPoiMarkers() {
        if (markersLoaded || !mapReady || !dbReady) return
        for (poi in pois) {
            if (poi.location == null) continue
            val poiPosition = LatLng(poi.location.latitude, poi.location.longitude)
            mMap.addMarker(MarkerOptions()
                .position(poiPosition)
                .title(poi.name)
            )
        }
        markersLoaded = true
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SWANSEA_LOCATION, DEFAULT_ZOOM))
        mapReady = true
        displayPoiMarkers()
    }

    /**
     * Populate the toolbar menu with the home screen actions.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        val isStandardUser = false
        val isAdminUser = false
        val isLoggedIn = isStandardUser || isAdminUser

        val loginAction = menu?.findItem(R.id.action_login)
        if (loginAction != null) loginAction.isVisible = !isLoggedIn

        val logoutAction = menu?.findItem(R.id.action_logout)
        if (logoutAction != null) logoutAction.isVisible = isLoggedIn

        val emailVisitedAction = menu?.findItem(R.id.action_email_visited)
        if (emailVisitedAction != null) emailVisitedAction.isVisible = isLoggedIn

        val addPoiAction = menu?.findItem(R.id.action_add)
        if (addPoiAction != null) addPoiAction.isVisible = isAdminUser

        return super.onCreateOptionsMenu(menu)
    }
}
