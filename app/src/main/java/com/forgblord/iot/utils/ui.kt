package com.forgblord.iot.utils

import android.content.Context
import android.widget.ProgressBar
import com.forgblord.iot.R

val NETWORK_STATE: Map<String, Pair<String, Int>> = mapOf(
    "start" to Pair("Connecting...", ProgressBar.VISIBLE),
    "stop" to Pair("Home", ProgressBar.INVISIBLE)
)

/*fun getSoilColor(context: Context, percent: Int): Int {
    if (percent > 50) {
        return context.getColor(R.color.md_theme_errorContainer)
    }
    return context.getColor(R.color.md_theme_primary)
}

fun getColorByState(context: Context, value: Boolean): Int {
    if (value) {
        return context.getColor(R.color.md_theme_error)
    }
    return context.getColor(R.color.progressbar)
}*/

fun floatToPercentage(value: Float, min: Float, max: Float): Int = (((value - min) * 100) / (max - min)).toInt()