package com.forgblord.iot.models

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

data class SystemState(
    private val context: Context,
) {
    private val sensorPrefs = context.getSharedPreferences("sensors", AppCompatActivity.MODE_PRIVATE)
    private val sensorPrefsEditor = sensorPrefs.edit()

    private var _soil = Soil(
        value = sensorPrefs.getInt("soil_value", 0),
        percent = sensorPrefs.getInt("soil_percent", 0)
    )

    val soil: Soil
        get() = _soil

    private var _water = Water(
        level = sensorPrefs.getFloat("water_level", 0f),
        percent = sensorPrefs.getInt("water_percent", 0)
    )

    val water: Water
        get() = _water

    var lastCheck: String? = sensorPrefs.getString("last_check", "01/01 00:00")

    fun updateState(soil: Soil, water: Water, date: String) {
        _soil = soil.copy()
        _water = water.copy()
        lastCheck = date

        sensorPrefsEditor.apply {
            putInt("soil_value", soil.value)
            putInt("soil_percent", soil.percent)

            putFloat("water_level", water.level)
            putInt("water_percent", water.percent)

            putString("last_check", date)
            apply()
        }
    }
}
