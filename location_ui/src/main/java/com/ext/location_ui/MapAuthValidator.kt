package com.ext.location_ui

import android.content.Context
import android.content.pm.PackageManager

object MapAuthValidator {

    private var providedApiKey: String? = null

    /** Called from app */
    fun provideApiKey(key: String) {
        providedApiKey = key.trim()
    }

    /** Read key from AndroidManifest */
    private fun getManifestApiKey(context: Context): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
    }

    /** FINAL validation */
    fun canAccessMap(context: Context): Boolean {
        val manifestKey = getManifestApiKey(context)

        return !providedApiKey.isNullOrBlank()
                && !manifestKey.isNullOrBlank()
                && providedApiKey == manifestKey
    }
}
