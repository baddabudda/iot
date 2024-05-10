package com.forgblord.iot.models

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

data class Config(
    private val context: Context,
) {
    private val configPrefs = context.getSharedPreferences("config", AppCompatActivity.MODE_PRIVATE)
    private val configPrefsEditor = configPrefs.edit()

    var config = ConfigRequest(
        waterMax = configPrefs.getFloat("water_max", 22f),
        soilMin = configPrefs.getInt("soilMin", 300),
        soilMax = configPrefs.getInt("soilMax", 3500),
        moistThreshold = configPrefs.getInt("moistThreshold", 2000),
        checkInterval = configPrefs.getInt("checkInterval", 10),
        wateringDuration = configPrefs.getInt("wateringDuration", 3000)
    )

    fun updateConfig(newConfig: ConfigRequest) {
        config = newConfig.copy()

        configPrefsEditor.apply {
            putFloat("water_max", config.waterMax)
            putInt("soilMin", config.soilMin)
            putInt("soilMax", config.soilMax)
            putInt("moistThreshold", config.moistThreshold)
            putInt("checkInterval", config.checkInterval)
            putInt("wateringDuration", config.wateringDuration)

            apply()
        }
    }
}
