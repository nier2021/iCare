package com.docter.icare.data.repositories

import android.content.Context
import android.util.Log
import com.docter.icare.data.entities.view.DeviceEntity
import com.docter.icare.data.entities.webSocket.SocketUpdate
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.network.api.webSocket.WebServices
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.*
import kotlinx.coroutines.channels.Channel

class RadarRepository (
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
) : SafeApiRequest(resource) {

    fun getDeviceAccountId() = preference.getInt(RADAR_DEVICE_ACCOUNT_ID,-1)

    //找裝置名稱是否有溫感器 没有温度的编号开头是TMOT04V1，有温度编号的是TMOT04V2
    fun isHasTemperature() : Boolean{
        val getRadarName = preference.getString(RADAR_DEVICE_NAME,"")
        val isHasTemperature = getRadarName.isNotBlank() && getRadarName.contains("V2")
        preference.set(HAS_RADAR_TEMPERATURE,isHasTemperature)
        return isHasTemperature
    }

    fun getHasTemperature() =  preference.getBoolean(HAS_RADAR_TEMPERATURE,false)

    fun cleanTemperature() =  preference.set(RADAR_TEMPERATURE,"")

//    //    @ExperimentalCoroutinesApi
//    fun startSocket(context: Context, accountId: Int): Channel<SocketUpdate> =
//        webServices.startSocket( context,accountId)
//
//    //    @ExperimentalCoroutinesApi
//    fun closeSocket() = webServices.stopSocket()

}