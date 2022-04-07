package com.example.swanseahistoryapp

import com.google.firebase.firestore.GeoPoint

data class PointOfInterest(val id : String, val name : String?, val address : String?,
                      val description : String?, val location : GeoPoint?, val imageURL : String?) {
}