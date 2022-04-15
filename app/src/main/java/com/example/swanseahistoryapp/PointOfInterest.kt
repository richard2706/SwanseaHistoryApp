package com.example.swanseahistoryapp

import android.os.Bundle
import com.google.firebase.firestore.GeoPoint

data class PointOfInterest(val id : String, val name : String?, val address : String?,
                      val description : String?, val location : GeoPoint?, val imageURL : String?) {

    /**
     * Returns a bundle of all PoI data. Used for passing PointOfInterest objects between
     * activities.
     */
    fun toBundle() : Bundle {
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putString("name", name)
        bundle.putString("address", address)
        bundle.putString("description", description)
        bundle.putString("imageURL", imageURL)

        val hasLocation = location != null
        bundle.putBoolean("hasLocation", hasLocation)
        if (hasLocation) {
            bundle.putDouble("latitude", location!!.latitude)
            bundle.putDouble("longitude", location!!.longitude)
        }
        return bundle
    }
}
