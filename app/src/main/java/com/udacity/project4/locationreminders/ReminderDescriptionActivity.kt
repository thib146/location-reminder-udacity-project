package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityReminderDescriptionBinding

    private lateinit var map: GoogleMap
    lateinit var reminderDataItem: ReminderDataItem

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        const val DEF_LAT = 40.734964
        const val DEF_LONG = -73.991140

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)

        reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        binding.reminderDataItem = reminderDataItem

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        showLocationPoi(map)
        setMapStyle(map)
    }

    private fun showLocationPoi(map: GoogleMap) {
        val zoomLevel = 15f
        val locationLatLng = LatLng(reminderDataItem.latitude ?: DEF_LAT, reminderDataItem.longitude ?: DEF_LONG)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoomLevel))
        map.uiSettings.apply {
            isScrollGesturesEnabled = false
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
        }
        map.addMarker(
            MarkerOptions()
                .position(locationLatLng)
                .title(reminderDataItem.title)
        )
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(SelectLocationFragment.TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(SelectLocationFragment.TAG, "Can't find map style. Error: ", e)
        }
    }
}