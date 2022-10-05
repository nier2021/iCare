package com.docter.icare.data.repositories

import android.content.Context
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
    private val webServices: WebServices
) : SafeApiRequest(resource) {

    fun getDeviceAccountId() = preference.getInt(RADAR_DEVICE_ACCOUNT_ID,-1)

    //    @ExperimentalCoroutinesApi
    fun startSocket(context: Context, accountId: Int): Channel<SocketUpdate> =
        webServices.startSocket( context,accountId)

    //    @ExperimentalCoroutinesApi
//    fun closeSocket() {
//        webServices.stopSocket()
//    }
    fun closeSocket() = webServices.stopSocket()

}