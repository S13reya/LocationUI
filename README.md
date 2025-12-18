# **LocationUI**
---

A custom **Location Picker View** library for Android that integrates Google Maps, allowing users to select locations with ease. It provides a search bar, shows selected addresses, allows camera zoom and rotation, and supports customizable attributes.

---

# **✨ Features**

- Display Google Map with a centered pin.

- Show current location with a blue dot.

- Search for a location by name.

- Display selected address dynamically.

- Zoom controls and map rotation.

- Fully customizable via XML attributes or Kotlin functions.

- API key validation for Google Maps

  # **Preview**
---
<p align="center">
  <img src="https://github.com/user-attachments/assets/51b0e437-cc67-4e62-a3f4-a8951db3dde5"
       alt="Demo GIF"
       width="200">



</p>

---

##  **⚡ Installation**

**Step 1:** Add JitPack repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2:** Add dependency to your app module `build.gradle`:
```
dependencies {
	        implementation 'com.github.Excelsior-Technologies-Community:AndroidCustomizableSeekBar:1.0.0'

 }
```

## **📦 Dependencies**
```
// Google Maps
implementation "com.google.android.gms:play-services-maps:18.2.0"
implementation "com.google.android.gms:play-services-location:21.3.0"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"

// Lifecycle
implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"

// AndroidX & UI
implementation "androidx.core:core-ktx:1.12.0"
implementation "androidx.appcompat:appcompat:1.7.0"
implementation "com.google.android.material:material:1.12.0"
implementation "androidx.constraintlayout:constraintlayout:2.2.0"

```

## **📦 Permissions**
Add these permissions to your AndroidManifest.xml:

```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

```

## **Google Maps API Key**

To use this library, **you must provide your own Google Maps API key.**

Add the key in your **AndroidManifest.xml** inside the <application> tag:


```

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY"/>


```

## **MainActivity**

```
locationPickerView.provideApiKey("YOUR_GOOGLE_MAPS_API_KEY")

```


 ## **Add LocationPickerView to XML**
```
<com.ext.location_ui.LocationPickerView
    android:id="@+id/locationPickerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:initialLatitude="23.0225"
    app:initialLongitude="72.5714"
    app:initialZoom="15"
    app:showSearchBar="true"
    app:showSelectedAddress="true"
    app:showZoomControls="true"
    app:enableMapRotation="true"
    app:pinColor="@color/red"/>
```
## **MainActivity code**
```
class MainActivity : AppCompatActivity() {

    private lateinit var locationPickerView: LocationPickerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationPickerView = findViewById(R.id.locationPickerView)

        // Provide your API key
        locationPickerView.provideApiKey("YOUR_GOOGLE_MAPS_API_KEY")

        // Set location callback
        locationPickerView.setLocationCallback(object : LocationPickerCallback {
            override fun onLocationSelected(latitude: Double, longitude: Double, address: String) {
                Toast.makeText(this@MainActivity, "Selected: $address\nLat: $latitude, Lng: $longitude", Toast.LENGTH_LONG).show()
            }

            override fun onLocationChanged(latitude: Double, longitude: Double, address: String) {}
            override fun onError(error: String) { Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show() }
        })

        // Initialize the location picker
        locationPickerView.initialize(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPickerView.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
```

## 📄 License
```
MIT License

Copyright (c) 2025 [Your Name or Organization]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

 






