package com.docter.icare.data.bleUtil.model

data class RadarData(
    var radarState: String,
    var distance: String,
    var bedState: String,
    var breathState: Int,
    var heartRate: Int,
    var createDate: String
)
