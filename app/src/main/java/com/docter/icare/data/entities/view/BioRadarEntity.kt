package com.docter.icare.data.entities.view

data class BioRadarEntity(
    val accountId: String = "",//會員id
    val time: String = "",//數據時間
    val radar_state: String = "",//正常|待機|異常
    val distance: String = "",//距離
    val heart_rate: String = "",//心率
    val bed_state: String = "",//未離床|正躺|側躺|坐在床邊
    val breath_state: String = "",//呼吸頻率
)
