package com.egorhoot.chomba.pages.user.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import javax.inject.Inject

class CameraManager @Inject constructor(
    private val context: Context,
) {
    private val sharedPref = context.getSharedPreferences(
        "com.egorhoot.chomba",
        Context.MODE_PRIVATE
    )

    private fun hasCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun cameraPermissionDenied(): Boolean {
        if (hasCameraPermission()) {
            with(sharedPref.edit()) {
                putBoolean("cameraPermissionDenied", false)
                apply()
            }
        }
        return sharedPref.getBoolean("cameraPermissionDenied", false)
    }

    fun cameraPermissionGranted(): Boolean {
        return hasCameraPermission()
    }

    fun onPermissionDenied() {
        if (cameraPermissionDenied()) return
        with(sharedPref.edit()) {
            putBoolean("cameraPermissionDenied", true)
            apply()
        }
    }
}