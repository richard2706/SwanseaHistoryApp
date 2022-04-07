package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.swanseahistoryapp.databinding.ActivityMapsBinding
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.reflect.typeOf

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

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
    private val db = Firebase.firestore
    private lateinit var pois: List<PointOfInterest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        db.collection(POI_COLLECTION_NAME).get()
            .addOnSuccessListener { result -> parsePois(result) }
            .addOnFailureListener { exception -> Log.w("firebase-log", exception) }
    }

    /**
     * Stores the data from the database in a list of PointOfInterest objects.
     */
    fun parsePois(result : QuerySnapshot) {
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
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SWANSEA_LOCATION, DEFAULT_ZOOM))
    }
}
