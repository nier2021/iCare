package com.docter.icare.data.entities.device

import androidx.lifecycle.MutableLiveData

data class DeviceInfoEntity(
    val radarDeviceMac: String ="",
    val radarDeviceName: String ="",
    val airDeviceMac: String ="",
    val airDeviceName: String ="",
    var bedType: Int = -1,
    var isWifiBind :Boolean = false,
    val wifiAccount: String ="",
    val wifiPassword: String ="",
    var isRadarBind: Boolean = false,
    var isAirBind: Boolean = false

)


//    deviceEntity.deviceMac = getString(RADAR_DEVICE_MAC)
//            deviceEntity.deviceName = getString(RADAR_DEVICE_NAME,)
////                Log.i("DeviceRepository","getDeviceInfo name=>${deviceEntity.deviceName}")
//            deviceEntity.wifiAccount.value = getString(WIFI_NAME)
//            deviceEntity.wifiPassword.value = getString(WIFI_PASS)
//            deviceEntity.isWifiBind.value = getBoolean(IS_WIFI)
//            deviceEntity.bedType = getInt(BED_TYPE,-1)
//        }
//    }else{
//        with(preference){
//            deviceEntity.deviceMac =  getString(AIR_DEVICE_MAC)
//            deviceEntity.deviceName = getString(AIR_DEVICE_NAME)
//            deviceEntity.deviceAccountId = getString(WIFI_NAME)
//            deviceEntity.deviceAccountId = getString(WIFI_PASS)
//            deviceEntity.deviceAccountId = getString(IS_WIFI)