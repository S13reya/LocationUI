package com.ext.location_ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import java.io.IOException
import java.util.Locale

class LocationPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnMapReadyCallback {

    private val etSearch: EditText
    private val btnSearch: ImageButton
    private val tvSelectedAddress: TextView
    private val tvSelectedLocationTitle: TextView
    private val mapView: MapView
    private val searchContainer: CardView
    private val addressContainer: CardView
    private val ivCenterPin: ImageView

    // ---------- MAP ----------
    private var googleMap: GoogleMap? = null
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // ---------- DATA ----------
    private var currentLatLng: LatLng? = null
    private var currentAddress: String = ""
    private var callback: LocationPickerCallback? = null
    private var activity: Activity? = null

    private var providedApiKey: String? = null

    private var pendingInitialLat: Double? = null
    private var pendingInitialLng: Double? = null
    private var pendingInitialZoom: Float? = null

    // Custom attributes
    private var showSearchBar: Boolean = true
    private var showSelectedAddress: Boolean = true
    private var pinColor: Int = 0xFFFF0000.toInt()
    private var showZoomControls: Boolean = true
    private var enableMapRotation: Boolean = false
    private var showMyLocationButton: Boolean = true
    private var searchIcon: Int = 0  // Will be set from XML or use default
    private var searchIconWidth: Int = -1  // -1 means use default (wrap_content)
    private var searchIconHeight: Int = -1  // -1 means use default (wrap_content)
    private var searchTextColor: Int = 0xFF000000.toInt()
    private var searchHintColor: Int = 0xFF666666.toInt()
    private var selectedLocationTitleColor: Int = 0xFF666666.toInt()
    private var selectedLocationAddressColor: Int = 0xFF333333.toInt()

    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_location_picker, this, true)

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress)
        tvSelectedLocationTitle = findViewById(R.id.tvSelectedLocationTitle)
        mapView = findViewById(R.id.mapView)
        searchContainer = findViewById(R.id.searchContainer)
        addressContainer = findViewById(R.id.addressContainer)
        ivCenterPin = findViewById(R.id.ivCenterPin)

        // Read custom attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.LocationPickerView)

            pendingInitialLat = if (typedArray.hasValue(R.styleable.LocationPickerView_initialLatitude)) {
                typedArray.getFloat(R.styleable.LocationPickerView_initialLatitude, 23.0225f).toDouble()
            } else null

            pendingInitialLng = if (typedArray.hasValue(R.styleable.LocationPickerView_initialLongitude)) {
                typedArray.getFloat(R.styleable.LocationPickerView_initialLongitude, 72.5714f).toDouble()
            } else null

            pendingInitialZoom = if (typedArray.hasValue(R.styleable.LocationPickerView_initialZoom)) {
                val zoom = typedArray.getFloat(R.styleable.LocationPickerView_initialZoom, 15f)
                zoom.coerceIn(2f, 21f)
            } else null

            showSearchBar = typedArray.getBoolean(R.styleable.LocationPickerView_showSearchBar, true)
            showSelectedAddress = typedArray.getBoolean(R.styleable.LocationPickerView_showSelectedAddress, true)
            pinColor = typedArray.getColor(R.styleable.LocationPickerView_pinColor, 0xFFFF0000.toInt())
            showZoomControls = typedArray.getBoolean(R.styleable.LocationPickerView_showZoomControls, true)
            enableMapRotation = typedArray.getBoolean(R.styleable.LocationPickerView_enableMapRotation, false)
            showMyLocationButton = typedArray.getBoolean(R.styleable.LocationPickerView_showMyLocationButton, true)

            // Read search icon (0 means not provided, will use existing icon from layout)
            searchIcon = typedArray.getResourceId(R.styleable.LocationPickerView_searchIcon, 0)

            // Read search icon dimensions
            searchIconWidth = typedArray.getDimensionPixelSize(R.styleable.LocationPickerView_searchIconWidth, -1)
            searchIconHeight = typedArray.getDimensionPixelSize(R.styleable.LocationPickerView_searchIconHeight, -1)

            // Read text colors
            searchTextColor = typedArray.getColor(R.styleable.LocationPickerView_searchTextColor, 0xFF000000.toInt())
            searchHintColor = typedArray.getColor(R.styleable.LocationPickerView_searchHintColor, 0xFF666666.toInt())
            selectedLocationTitleColor = typedArray.getColor(R.styleable.LocationPickerView_selectedLocationTitleColor, 0xFF666666.toInt())
            selectedLocationAddressColor = typedArray.getColor(R.styleable.LocationPickerView_selectedLocationAddressColor, 0xFF333333.toInt())

            typedArray.recycle()
        }

        setupUI()
        applyVisibilitySettings()
        applyPinColor()
        applyCustomStyles()
    }

    // ================= PUBLIC =================

    fun provideApiKey(apiKey: String) {
        providedApiKey = apiKey.trim()
    }

    fun setLocationCallback(callback: LocationPickerCallback) {
        this.callback = callback
    }

    fun setShowSearchBar(show: Boolean) {
        showSearchBar = show
        applyVisibilitySettings()
    }

    fun setShowSelectedAddress(show: Boolean) {
        showSelectedAddress = show
        applyVisibilitySettings()
    }

    fun setPinColor(color: Int) {
        pinColor = color
        applyPinColor()
    }

    fun setShowZoomControls(show: Boolean) {
        showZoomControls = show
        googleMap?.uiSettings?.isZoomControlsEnabled = show
    }

    fun setEnableMapRotation(enable: Boolean) {
        enableMapRotation = enable
        googleMap?.uiSettings?.isRotateGesturesEnabled = enable
        googleMap?.uiSettings?.isCompassEnabled = enable
    }

    fun setShowMyLocationButton(show: Boolean) {
        showMyLocationButton = show
        googleMap?.uiSettings?.isMyLocationButtonEnabled = show
    }

    fun setSearchIconSize(width: Int, height: Int) {
        searchIconWidth = width
        searchIconHeight = height
        applySearchIconSize()
    }

    fun initialize(activity: Activity) {
        this.activity = activity

        if (!validateApiKey()) {
            showBlockingError(
                "Google Maps API key missing or invalid.\n" +
                        "Please add your API key in AndroidManifest.xml"
            )
            callback?.onError("API key validation failed")
            return
        }

        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    // ================= INTERNAL =================

    private fun applyVisibilitySettings() {
        searchContainer.visibility = if (showSearchBar) View.VISIBLE else View.GONE
        addressContainer.visibility = if (showSelectedAddress) View.VISIBLE else View.GONE

        googleMap?.let {
            val paddingBottom = if (showSelectedAddress) 300 else 16
            it.setPadding(0, 0, 0, paddingBottom)
        }
    }

    private fun applyPinColor() {
        ivCenterPin.setColorFilter(pinColor, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun applyCustomStyles() {
        // Apply search icon only if provided in XML, otherwise keep the existing icon from layout
        if (searchIcon != 0) {
            btnSearch.setImageResource(searchIcon)
        }

        // Apply search icon size
        applySearchIconSize()

        // Apply search text colors
        etSearch.setTextColor(searchTextColor)
        etSearch.setHintTextColor(searchHintColor)

        // Apply selected location colors
        tvSelectedLocationTitle.setTextColor(selectedLocationTitleColor)
        tvSelectedAddress.setTextColor(selectedLocationAddressColor)
    }

    private fun applySearchIconSize() {
        if (searchIconWidth != -1 || searchIconHeight != -1) {
            val layoutParams = btnSearch.layoutParams

            if (searchIconWidth != -1) {
                layoutParams.width = searchIconWidth
            }

            if (searchIconHeight != -1) {
                layoutParams.height = searchIconHeight
            }

            btnSearch.layoutParams = layoutParams

            // Adjust scale type to fit the icon properly within the new dimensions
            btnSearch.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    private fun validateApiKey(): Boolean {
        if (providedApiKey.isNullOrBlank()) return false

        val manifestKey = try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }

        return !manifestKey.isNullOrBlank() && manifestKey == providedApiKey
    }

    private fun showBlockingError(message: String) {
        removeAllViews()
        addView(TextView(context).apply {
            text = message
            gravity = Gravity.CENTER
            textSize = 16f
            setPadding(32, 32, 32, 32)
        })
    }

    private fun setupUI() {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearch.text.toString())
                true
            } else false
        }

        btnSearch.setOnClickListener {
            searchLocation(etSearch.text.toString())
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap!!.uiSettings.isMyLocationButtonEnabled = showMyLocationButton
        googleMap!!.uiSettings.isZoomControlsEnabled = showZoomControls
        googleMap!!.uiSettings.isRotateGesturesEnabled = enableMapRotation
        googleMap!!.uiSettings.isCompassEnabled = enableMapRotation

        val paddingBottom = if (showSelectedAddress) 300 else 16
        googleMap!!.setPadding(0, 0, 0, paddingBottom)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap!!.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val latLng = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    LatLng(pendingInitialLat ?: 23.0225, pendingInitialLng ?: 72.5714)
                }

                val zoom = pendingInitialZoom ?: 15f
                currentLatLng = latLng
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
            }
        } else {
            val lat = pendingInitialLat ?: 23.0225
            val lng = pendingInitialLng ?: 72.5714
            val zoom = pendingInitialZoom ?: 15f
            val latLng = LatLng(lat, lng)
            currentLatLng = latLng
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

            checkLocationPermission()
        }

        googleMap!!.setOnCameraIdleListener {
            googleMap!!.cameraPosition.target.let {
                currentLatLng = it
                updateAddressFromLocation(it)
            }
        }
    }

    private fun checkLocationPermission() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun searchLocation(query: String) {
        if (query.isBlank()) return

        viewScope.launch {
            val address = withContext(Dispatchers.IO) {
                try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocationName(query, 1)
                        ?.firstOrNull()
                } catch (e: IOException) {
                    null
                }
            }

            address?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                )
                currentAddress = it.getAddressLine(0)
                updateAddressDisplay(currentAddress)
                callback?.onLocationChanged(latLng.latitude, latLng.longitude, currentAddress)
            }
        }
    }

    private fun updateAddressFromLocation(latLng: LatLng) {
        viewScope.launch {
            val address = withContext(Dispatchers.IO) {
                try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(latLng.latitude, latLng.longitude, 1)
                        ?.firstOrNull()
                } catch (e: IOException) {
                    null
                }
            }

            currentAddress = address?.getAddressLine(0)
                ?: "Lat:${latLng.latitude}, Lng:${latLng.longitude}"

            updateAddressDisplay(currentAddress)
            callback?.onLocationChanged(latLng.latitude, latLng.longitude, currentAddress)
        }
    }

    private fun updateAddressDisplay(address: String) {
        if (showSelectedAddress) {
            tvSelectedAddress.text = address
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            if (showMyLocationButton) {
                googleMap?.uiSettings?.isMyLocationButtonEnabled = true
            }
        }
    }
}