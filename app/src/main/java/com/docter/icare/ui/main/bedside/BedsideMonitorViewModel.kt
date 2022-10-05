package com.docter.icare.ui.main.bedside

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.docter.icare.R
import com.docter.icare.data.entities.view.BedsideMonitorEntity
import com.docter.icare.data.entities.webSocket.SocketUpdate
import com.docter.icare.data.repositories.RadarRepository
import com.docter.icare.utils.Coroutines.io
import com.docter.icare.utils.toRadarShowDate
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers.io
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.TimeUnit

class BedsideMonitorViewModel(
    private val radarRepository: RadarRepository,
) : ViewModel() {

    val entity = BedsideMonitorEntity()

    val throwMessage = MutableLiveData("")

    fun getDeviceAccountId() = radarRepository.getDeviceAccountId()

    fun stopSocket() = radarRepository.closeSocket()

    fun startSocket(context: Context ,accountId: Int): Channel<SocketUpdate> =
        radarRepository.startSocket(context,accountId)

    fun socketData(context: Context, data: SocketUpdate) {
//        if (data.bioRadar!!.radar_state == "正常") {
//            Log.i("BedsideMonitorViewModel","socketData")
//            Log.i("BedsideMonitorViewModel","bed_state=>${data.bioRadar!!.bed_state}")
            val a= data.bioRadar!!.bed_state
//            if (data.bioRadar.distance.toDouble() > 0.1  && data.bioRadar.bed_state != "離床"){
            if (data.bioRadar.distance.toDouble() > 10 && data.bioRadar.bed_state != "離床"){
                when (data.bioRadar.bed_state) {
                    "正躺", "側躺"-> {
                        with(entity) {
                            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_in_bed)
                            frameBedStatus.value =
                                ContextCompat.getDrawable(context, R.drawable.icon_in_bed_round)
                            textBedStatus.value = context.getString(
                                if (data.bioRadar.bed_state == "正躺") R.string.lying_lie else R.string.lie_down
                            )
                            textColorBedStatus.value =
                                ContextCompat.getColor(context, R.color.radar_in_bed_color)
                            time.value = data.bioRadar.time.toRadarShowDate()
                            heartRate.value = data.bioRadar.heart_rate
                            heartRateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                            //心率icon與文字顏色判斷等後端送上下限值在做
                            breathState.value = data.bioRadar.breath_state
                            breathStateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                            //呼吸頻率icon與文字顏色判斷等後端送上下限值在做
                            bodyTemperature.value = if (data.bioRadar.temperature.isNotBlank()) String.format("%.1f", data.bioRadar.temperature.toFloat()) else ""
//                            bodyTemperature.value = getRandomBt()
                            if (bodyTemperature.value?.isNotBlank() == true) {
                                when(bodyTemperature.value!!.toFloat()) {//傳過來小數兩位 顯示小數一位 並重心排版
                                    -1f ->{
                                        bodyTemperatureTextColor.value = ContextCompat.getColor(context, R.color.radar_out_bed_color)
                                        iconBodyTemperature.value =  ContextCompat.getDrawable(context, R.drawable.icon_body_temperature_blue)
                                    }
                                    in 35f..38f -> {
                                        bodyTemperatureTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                                        iconBodyTemperature.value =  ContextCompat.getDrawable(context, R.drawable.icon_body_temperature_blue)
                                    }
                                    else ->{
                                        bodyTemperatureTextColor.value = ContextCompat.getColor(context, R.color.radar_out_bed_color_alert)
                                        iconBodyTemperature.value =  ContextCompat.getDrawable(context, R.drawable.icon_body_temperature_red)
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        //坐在床邊
                        with(entity) {
                            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_side_bed)
                            frameBedStatus.value =
                                ContextCompat.getDrawable(context, R.drawable.icon_side_bed_round)
                            textBedStatus.value = context.getString( R.string.side_bed)
                            textColorBedStatus.value =
                                ContextCompat.getColor(context, R.color.radar_side_bed_color)
                            time.value = data.bioRadar.time.toRadarShowDate()
                            heartRate.value = ""
//                            heartRateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                            breathState.value = ""
//                            breathStateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                            bodyTemperature.value = if ( data.bioRadar.temperature.isNotBlank()) "-1" else ""
//                            bodyTemperatureTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                        }
                    }
                }
            }else{
                //離床 距離小於等於10cm
                with(entity) {
                    iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed)
                    frameBedStatus.value =
                        ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed_round)
                    textBedStatus.value = context.getString(R.string.out_of_bed)
                    textColorBedStatus.value =
                        ContextCompat.getColor(context, R.color.radar_out_bed_color)
                    time.value = data.bioRadar.time.toRadarShowDate()
                    heartRate.value = ""
//                    heartRateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                    breathState.value = ""
//                    breathStateTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                    bodyTemperature.value = if ( data.bioRadar.temperature.isNotBlank()) "-1" else ""
//                    bodyTemperatureTextColor.value = ContextCompat.getColor(context, R.color.radar_side_bed_color)
                }
            }

//        }
//        else {
//            socketError(context)
//        }
    }



    //後端傳送4狀態正躺 側躺 坐在床邊 離床
    //狀態顏色&&圖片 在床&&側躺＆＆ 藍色 圖相同 值：狀態顯示文字不同 ; 離床&&無人：顏色＝>灰色 圖不同 無值--(直接顯示不管是否有收到呼吸等值);
    /*
    fun socketData(context: Context, data: SocketUpdate) {
        if (data.bioRadar!!.radar_state == "正常") {
        Log.i("BedsideMonitorViewModel","socketData")
        Log.i("BedsideMonitorViewModel","bed_state=>${data.bioRadar.bed_state}")
//        Log.i("BedsideMonitorViewModel","is_bed_state 正躺=>${data.bioRadar.bed_state=="正躺"}")
//        Log.i("BedsideMonitorViewModel","is_bed_state 側躺=>${data.bioRadar.bed_state=="側躺"}")
//        Log.i("BedsideMonitorViewModel","is_bed_state 坐在床邊=>${data.bioRadar.bed_state=="坐在床邊"}")
//        Log.i("BedsideMonitorViewModel","is_bed_state 離床=>${data.bioRadar.bed_state=="離床"}")
            if (data.bioRadar.distance.toDouble() > 0.1 ){
                when (data.bioRadar.bed_state) {
                    "正躺", "側躺", "坐在床邊" -> {
                        with(entity) {
                            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_in_bed)
                            frameBedStatus.value =
                                ContextCompat.getDrawable(context, R.drawable.icon_in_bed_round)
//                        textBedStatus.value = context.getString(R.string.on_bed)
                            textBedStatus.value = context.getString(
                                when (data.bioRadar.bed_state) {
                                    "正躺" -> R.string.lying_lie
                                    "側躺" -> R.string.lie_down
                                    else -> R.string.side_bed
                                }
                            )
                            textColorBedStatus.value =
                                ContextCompat.getColor(context, R.color.radar_in_bed_color)
                            time.value = data.bioRadar.time.toRadarShowDate()
                            heartRate.value = data.bioRadar.heart_rate
                            breathState.value = data.bioRadar.breath_state
                        }
                    }
                    else -> {
                        //離床
                        with(entity) {
                            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed)
                            frameBedStatus.value =
                                ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed_round)
                            textBedStatus.value = context.getString(R.string.out_of_bed)
                            textColorBedStatus.value =
                                ContextCompat.getColor(context, R.color.radar_out_bed_color)
                            time.value = "--"
                            heartRate.value = "--"
                            breathState.value = "--"
                        }
                    }
                }
            }else{
                //離床 距離小於等於0.1
                with(entity) {
                    iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed)
                    frameBedStatus.value =
                        ContextCompat.getDrawable(context, R.drawable.icon_get_out_bed_round)
                    textBedStatus.value = context.getString(R.string.out_of_bed)
                    textColorBedStatus.value =
                        ContextCompat.getColor(context, R.color.radar_out_bed_color)
                    time.value = "--"
                    heartRate.value = "--"
                    breathState.value = "--"
                }
            }

        }
        else {
            socketError(context)
        }
    }
    */


    fun socketError(context: Context) {
        Log.i("BedsideMonitorViewModel","socketError")
        with(entity) {
            breathState.value = ""
            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_error_device)
            frameBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_unbind_device_round)
            textBedStatus.value = context.getString(R.string.error_text)
            textColorBedStatus.value = ContextCompat.getColor(context, R.color.radar_out_bed_color)
            time.value = ""
            heartRate.value= ""
            breathState.value = ""
            bodyTemperature.value = ""
        }
    }

    fun closeSocket(context: Context) {
        Log.i("BedsideMonitorViewModel","closeSocket")
        with(entity) {
            breathState.value = ""
            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_unbind_device)
            frameBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_unbind_device_round)
            textBedStatus.value = context.getString(R.string.radar_status_unbind_text)
            textColorBedStatus.value = ContextCompat.getColor(context, R.color.radar_out_bed_color)
            time.value = ""
            heartRate.value = ""
            breathState.value = ""
            bodyTemperature.value = ""
        }
    }

    fun bindDeviceShow(context: Context){
        Log.i("BedsideMonitorViewModel","closeSocket")
        with(entity) {
            breathState.value = ""
            iconBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_wifi_link)
            frameBedStatus.value = ContextCompat.getDrawable(context, R.drawable.icon_unbind_device_round)
            textBedStatus.value = context.getString(R.string.wifi_link_text)
            textColorBedStatus.value = ContextCompat.getColor(context, R.color.radar_out_bed_color)
            time.value = ""
            heartRate.value= ""
            breathState.value = ""
            bodyTemperature.value = ""
        }
    }

    private fun getRandomBt()= (30.. 40).random().toString()

}