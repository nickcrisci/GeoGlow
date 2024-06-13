package com.example.geoglow

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.geoglow.ui.theme.GeoGlowTheme
import com.example.geoglow.ui.screens.Navigation


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ColorViewModel>()
    private lateinit var mqttClient: MqttClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mqttClient = MqttClient(this)
        mqttClient.connect()

        setContent {
            GeoGlowTheme {
                Navigation(viewModel, mqttClient)
            }
        }

        var uniqueID = SharedPreferencesHelper.getUniqueID(this)
        if (uniqueID == null) {
            // Generate a new unique ID, save it, publish it
            uniqueID = IDGenerator.generateUniqueID()
            SharedPreferencesHelper.setUniqueID(this, uniqueID)
            mqttClient.publish(uniqueID, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.disconnect()
    }
}