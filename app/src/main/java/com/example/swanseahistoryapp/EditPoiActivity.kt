package com.example.swanseahistoryapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream

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
    private var pickedImageUri : Uri? = null
    private lateinit var previewImageView : ImageView
    private lateinit var removeImageButton : Button

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

        previewImageView = findViewById(R.id.image_view_selected)
        removeImageButton = findViewById(R.id.button_remove_image)
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
            R.id.action_save -> if (newPoi) saveNewPoi() else updatePoi()
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
     * Allows the user to choose an image for the PoI.
     */
    fun onSelectImageButtonClick(view : View) {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, 0)
    }

    /**
     * Allows the user to deselect the image for the PoI
     */
    fun onRemoveImageButtonClick(view : View) {
        previewImageView.visibility = View.GONE
        removeImageButton.visibility = View.GONE
        pickedImageUri = null
    }

    /**
     * Handle when a photo has been chosen. Stores the image URI and shows a preview
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val thumbnail: Bitmap? = data?.getParcelableExtra("data")
            pickedImageUri = data?.data
            previewImageView.setImageURI(pickedImageUri);
            previewImageView.visibility = View.VISIBLE
            removeImageButton.visibility = View.VISIBLE
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Save the new PoI to the database.
     */
    private fun saveNewPoi() {
        if (pickedImageUri == null) uploadPoiData(null)

        val storage = Firebase.storage.reference
        val imageReference = storage.child("poi_images/$pickedImageUri?.lastPathSegment")

        var uploadImageTask = imageReference.putFile(pickedImageUri!!)
        Log.i("add-poi-debug", "uploading")
        uploadImageTask.addOnSuccessListener {
            val imageURL = imageReference.downloadUrl.toString()
            uploadPoiData(imageURL)
            Log.i("add-poi-debug", imageURL)
        }
    }

    /**
     * Upload the PoI data to the database.
     */
    private fun uploadPoiData(imageURL : String?) {
        val location = if (pickedLocation != null)
            GeoPoint(pickedLocation!!.latitude, pickedLocation!!.longitude) else null
        val data = hashMapOf(
            "name" to findViewById<EditText>(R.id.field_poi_name).text.toString(),
            "address" to findViewById<EditText>(R.id.field_poi_address).text.toString(),
            "location" to location,
            "image_url" to imageURL,
            "description" to findViewById<EditText>(R.id.field_poi_description).text.toString()
        )

        db.collection(POI_COLLECTION).document().set(data)
            .addOnSuccessListener {
                finish()
            }
    }

    /**
     * Update the PoI in the database.
     */
    private fun updatePoi() {

    }
}