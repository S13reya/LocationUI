package com.ext.location_ui

interface LocationPickerCallback {
    /**
     * Called when user confirms location selection
     * @param latitude Selected latitude
     * @param longitude Selected longitude
     * @param address Address string of the selected location
     */
    fun onLocationSelected(latitude: Double, longitude: Double, address: String)

    /**
     * Called when map moves and location changes
     * @param latitude Current center latitude
     * @param longitude Current center longitude
     * @param address Address string of the current location
     */
    fun onLocationChanged(latitude: Double, longitude: Double, address: String)

    /**
     * Called when an error occurs
     * @param error Error message
     */
    fun onError(error: String)
}