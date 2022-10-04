package com.docter.icare.data.entities.view

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData

data class DeviceEntity(
    var deviceType: String = "",
    val type: MutableLiveData<Int> = MutableLiveData(RADAR),
    val backgroundDeviceBind: Drawable? = null,
    val iconDeviceBind: Drawable? = null,
    val deviceBindTitle: String = "",
    var deviceName: String = "",
//    var wifiAccount: String = "",
    var wifiPassword: String = "",
//    var isWifiBind :MutableLiveData<Boolean> = MutableLiveData(false),
    val wifiAccount: MutableLiveData<String> = MutableLiveData("") ,
//    val wifiPassword:MutableLiveData<String> = MutableLiveData("") ,
    var bedType: Int = -1,
//    var bedTypeName: String = "",
    val bedTypeName: MutableLiveData<String> = MutableLiveData("") ,
    var deviceMac: String = "",
    var deviceAccountId: String =""
//            android:background="@{entity.type == BloodPressureEntity.TYPE_PULSE_TRANSIT_TIME ? @color/colorPrimaryDark : @color/hintColor}"
//tools:background="@color/hintColor" />

){
    companion object{
        const val RADAR = 0
        const val AIR = 1
        const val ACTIVITY_MONITORING = 2
    }
}
