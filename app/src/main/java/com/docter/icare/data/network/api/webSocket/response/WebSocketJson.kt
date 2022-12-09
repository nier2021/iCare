package com.docter.icare.data.network.api.webSocket.response

import android.util.Log
import com.google.gson.Gson

class WebSocketJson {
    fun pusherSubscribeJson(value: String) : PusherSubscribeEntity {
//        Log.i("WebSocketJson","pusherSubscribeJson value=>$value")
        val toObject = Gson().fromJson(value,PusherSubscribeResponse::class.java)
//        Log.i("WebSocketJson","pusherSubscribeJson toObject=>$toObject")
        val toDataObject = Gson().fromJson(toObject.data,PusherSubscribeEntityData::class.java)
//        Log.i("WebSocketJson","pusherSubscribeJson toDataObject=>$toDataObject")
        return PusherSubscribeEntity(event = toObject.event, data = toDataObject   )
    }

    fun sendResponse(value: String):SendResponseEntity{
        return Gson().fromJson(value,SendResponseEntity::class.java)
    }

    fun responseJson(value: String) : RadarInfoEntity {
//        Log.i("WebSocketJson","responseJson value=>$value")
        val toRadarInfoResponse = Gson().fromJson(value,RadarInfoResponse::class.java)
//        Log.i("WebSocketJson","jsonToObject toRadarInfoResponse=>$toRadarInfoResponse")
//        Log.i("WebSocketJson","jsonToObject toRadarInfoResponse channel=>${toRadarInfoResponse.channel}")
//        Log.i("WebSocketJson","jsonToObject toRadarInfoResponse data=>${toRadarInfoResponse.data}")
        val totoRadarInfoData = Gson().fromJson(toRadarInfoResponse.data,RadarInfoData1::class.java)
//        Log.i("WebSocketJson","jsonToObject totoRadarInfoData accountId=>${totoRadarInfoData.accountId}")
//        Log.i("WebSocketJson","jsonToObject totoRadarInfoData status=>${totoRadarInfoData.status.breath_state}")
//        Log.i("WebSocketJson","jsonToObject toRadarInfoStatus=>${totoRadarInfoData.status}.")
        return RadarInfoEntity(channel = toRadarInfoResponse.channel, data = totoRadarInfoData, event = toRadarInfoResponse.event )
    }

}

data class PusherSubscribeResponse(
    val event: String,
    val data: String//
)

data class PusherSubscribeEntityData(
    val socket_id: String,
    val activity_timeout: String
)

data class PusherSubscribeEntity(
    val event: String,
    val data: PusherSubscribeEntityData
)

data class SendResponseEntity(
    val event: String,
    val channel: String
)

data class RadarInfoResponse(
    val channel: String,
    val data: String,//
    val event: String,
)

data class RadarInfoData1(
    val status: RadarInfoStatus2,//
    val accountId: String,
    val time: String,
)

data class RadarInfoStatus2(
//    val radar_state: String,
    val distance: String,
    val heart_rate: String,
    val bed_state: String,
    val breath_state: String,
    val temperature: String
)

data class RadarInfoEntity(
    val channel: String,
    val data: RadarInfoData1,
    val event: String,
)