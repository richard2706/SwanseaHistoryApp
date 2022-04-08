package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.firestore.GeoPoint

class PoiDetails : AppCompatActivity() {
    private var poi : PointOfInterest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poidetails)
        setSupportActionBar(findViewById(R.id.poi_details_toolbar))
        getPoiData()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.poi_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}