package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class EditPoiActivity : AppCompatActivity() {
    private var newPoi = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_poi)

        val extraData = intent.extras
        if (extraData != null) {
            newPoi = extraData!!.getBoolean("newPoi", true)
        }

        findViewById<Toolbar>(R.id.toolbar_edit_poi).title =
            if (newPoi) getString(R.string.title_add_poi) else getString(R.string.title_edit_poi)
    }
}