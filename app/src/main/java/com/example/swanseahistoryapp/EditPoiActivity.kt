package com.example.swanseahistoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
     * Save the new PoI to the database.
     */
    private fun saveNewPoi() {

    }

    /**
     * Update the PoI in the database.
     */
    private fun updatePoi() {

    }
}