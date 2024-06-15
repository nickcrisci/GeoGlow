package com.example.geoglow

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast
import androidx.core.content.ContextCompat


class PermissionHandler(private val context: Context) {
    fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted) {
            when (permission) {
                Manifest.permission.CAMERA -> {
                    Toast.makeText(context, "Camera permission must be granted to take pictures.", Toast.LENGTH_LONG).show()
                }
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    Toast.makeText(context, "GPS must be enabled to transmit colors.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}