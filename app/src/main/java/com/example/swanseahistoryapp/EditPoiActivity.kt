package com.example.swanseahistoryapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditPoiActivity : AppCompatActivity() {
    companion object {
        private const val POI_COLLECTION = "points_of_interest"
        private const val POI_NAME_FIELD = "name"
        private const val POI_ADDRESS_FIELD = "address"
        private const val POI_DESCRIPTION_FIELD = "description"
        private const val POI_LOCATION_FIELD = "location"
        private const val POI_IMAGE_URL_FIELD = "image_url"
    }

    private val db = Firebase.firestore
    private var newPoi = true

    private var pickedLocation : LatLng? = null

    // Saves the picked location from the location picker activity
    private val locationPickerForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                pickedLocation = result.data?.getParcelableExtra("pickedLocation")
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_poi)

        val extraData = intent.extras
        if (extraData != null) {
            newPoi = extraData!!.getBoolean("newPoi", true)
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar_edit_poi)
        setSupportActionBar(toolbar)
        toolbar.title =
            if (newPoi) getString(R.string.title_add_poi) else getString(R.string.title_edit_poi)
    }

    /**
     * Populates the options menu with the appropriate options dependent on if the user is adding or
     * editing a PoI.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_poi_edit, menu)
        menu?.findItem(R.id.action_delete)?.isVisible = !newPoi
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handle actions when each menu option is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> if (newPoi) saveNewPoi() else updatePoi()
            R.id.action_cancel -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Opens the location picker activity when the location picker button is clicked.
     */
    fun onChooseLocationButtonClick(view : View) {
        locationPickerForResult.launch(Intent(this, LocationPickerActivity::class.java))
    }

    /**
     * Save the new PoI to the database.
     */
    private fun saveNewPoi() {
        // upload image to firebase storage, get url

        val location = if (pickedLocation != null)
            GeoPoint(pickedLocation!!.latitude, pickedLocation!!.longitude) else null
//        val data = hashMapOf(
//            "name" to findViewById<EditText>(R.id.field_poi_name).text.toString(),
//            "address" to findViewById<EditText>(R.id.field_poi_address).text.toString(),
//            "location" to location,
//            "image_url" to ,
//            "description" to findViewById<EditText>(R.id.field_poi_description).text.toString()
        )
        db.collection(POI_COLLECTION).document().set
    }

    /**
     * Update the PoI in the database.
     */
    private fun updatePoi() {

    }
}