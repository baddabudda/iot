package com.forgblord.iot.models

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

data class Server(
    val context: Context,
) {
    private val serverPrefs = context.getSharedPreferences("server", AppCompatActivity.MODE_PRIVATE)
    private val serverPrefsEditor = serverPrefs.edit()

    fun getURL(): String? {
        return serverPrefs.getString("url", "http://192.168.0.123")
    }

    fun setURL(url: String) {
        serverPrefsEditor.putString("url", url).apply()
    }
}
