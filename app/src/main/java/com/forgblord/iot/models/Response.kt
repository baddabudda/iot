package com.forgblord.iot.models

data class Water(
    val level: Float,
    val percent: Int,
)

data class Soil(
    val value: Int,
    val percent: Int,
)

data class Response(
    val water: Water,
    val soil: Soil,
)

data class Message(
    val message: String,
)

data class ConfigRequest(
    var waterMax: Float,
    var soilMin: Int,
    var soilMax: Int,
    var moistThreshold: Int,
    var checkInterval: Int,
    var wateringDuration: Int,
)