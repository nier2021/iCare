package com.docter.icare.data.entities.view

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData

data class DeviceEntity(
    var deviceType: String = "",
    val backgroundDeviceBind: Drawable? = null,
    val iconDeviceBind: Drawable? = null,
    val deviceBindTitle: String = "",
    var deviceName: String = "",
//    val wifiAccount: String = "",
//    val wifiPassword: String = "",
    var isWifiBind :MutableLiveData<Boolean> = MutableLiveData(false),
    val wifiAccount: MutableLiveData<String> = MutableLiveData("") ,
    val wifiPassword:MutableLiveData<String> = MutableLiveData("") ,
    var bedType: Int = -1,
//    var bedTypeName: String = "",
    val bedTypeName: MutableLiveData<String> = MutableLiveData("") ,
    var deviceMac: String = "",
    var deviceAccountId: String =""
)
