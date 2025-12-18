package com.ext.locationuilibrary

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ext.location_ui.LocationPickerCallback
import com.ext.location_ui.LocationPickerView

class MainActivity : AppCompatActivity() {

    private lateinit var locationPickerView: LocationPickerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLocationPicker()
    }

    private fun setupLocationPicker() {
        locationPickerView = findViewById(R.id.locationPickerView)

        // ✅ USER PROVIDES OWN API KEY
        locationPickerView.provideApiKey("__REPLACE_WITH_YOUR_API_KEY__")

        // Optional initial location

        locationPickerView.setLocationCallback(object : LocationPickerCallback {
            override fun onLocationSelected(latitude: Double, longitude: Double, address: String) {
                Toast.makeText(
                    this@MainActivity,
                    "Selected: $address\nLat: $latitude, Lng: $longitude",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onLocationChanged(latitude: Double, longitude: Double, address: String) {}

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        })

        locationPickerView.initialize(this)
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPickerView.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
