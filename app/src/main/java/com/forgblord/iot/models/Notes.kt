package com.forgblord.iot.models

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

data class Notes(
    private val context: Context,
) {
    private val notePrefs = context.getSharedPreferences("notes", AppCompatActivity.MODE_PRIVATE)
    private val notePrefsEditor = notePrefs.edit()

    val plantInfo = mutableMapOf(
        "title" to notePrefs.getString("title", "Untitled"),
        "note" to notePrefs.getString("note", "")
    )

    fun updatePlantInfo(key: String, value: String) {
        plantInfo[key] = value
        notePrefsEditor.apply {
            putString(key, value)
            apply()
        }
    }
}
