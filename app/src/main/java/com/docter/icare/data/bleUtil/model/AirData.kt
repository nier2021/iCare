package com.docter.icare.data.bleUtil.model

data class AirData(
    var co2: Int,
    var ch2o: Int,
    var tvoc: Int,
    var pm25: Int,
    var pm10: Int,
    var temperature: Double,
    var humidity: Double
)