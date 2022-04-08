package com.example.swanseahistoryapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * Handles displaying the info window when a POI marker is clicked.
 */
class PoiMarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    /**
     * Populates the marker info window with content for the selected marker (given as a parameter).
     */
    override fun getInfoContents(marker: Marker): View? {
        val poi = marker?.tag as? PointOfInterest ?: return null

        val infoWindow = LayoutInflater.from(context)
            .inflate(R.layout.poi_marker_info_contents, null)
        infoWindow.findViewById<TextView>(R.id.info_title_text).text = poi.name
        return infoWindow
    }

    /**
     * Returns null to indicate that the default white info box should be used.
     */
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}
