package com.example.swanseahistoryapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.swanseahistoryapp.databinding.ActivityLocationPickerBinding

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener {

    companion object {
        private val SWANSEA_LOCATION = LatLng(51.6255408,-3.9655064)
        private const val DEFAULT_ZOOM = 11F
    }

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var pickedLocation : LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SWANSEA_LOCATION, DEFAULT_ZOOM))
        map.setOnMapClickListener(this)
    }

    /**
     * Return the clicked location to the previous activity.
     */
    override fun onMapClick(location: LatLng) {
        pickedLocation = location
        finish()
    }

    /**
     * Pass the picked location back to the previous activity.
     */
    override fun finish() {
        val data = Intent()
        data.putExtra("pickedLocation", pickedLocation)
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }
}