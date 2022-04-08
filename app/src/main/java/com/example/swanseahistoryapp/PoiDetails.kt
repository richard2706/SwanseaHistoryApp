package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.GeoPoint

class PoiDetails : AppCompatActivity() {
    private var poi : PointOfInterest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poidetails)
        setSupportActionBar(findViewById(R.id.poi_details_toolbar))
        getPoiData()
        displayPoiInfo()
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
        // display image
        // display visited message if visited
        if (poi!!.name != null) findViewById<Toolbar>(R.id.poi_details_toolbar).title = poi!!.name
        if (poi!!.address != null) {
            val addressText = findViewById<TextView>(R.id.address_text)
            addressText.text = poi!!.address
            addressText.visibility = View.VISIBLE
        }
        if (poi!!.description != null)
            findViewById<TextView>(R.id.description_text).text = poi!!.description
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.poi_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}