package com.docter.icare.data.entities.view

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData

data class BedsideMonitorEntity(
    val iconBedStatus: MutableLiveData<Drawable> = MutableLiveData(null),//中間圖示
    val frameBedStatus: MutableLiveData<Drawable> = MutableLiveData(null),//外框圖示
    val textBedStatus: MutableLiveData<String> = MutableLiveData(""),//床上狀態
    var textColorBedStatus: MutableLiveData<Int> = MutableLiveData(-1),//床上狀態顏色
    var time: MutableLiveData<String> = MutableLiveData(""),//資料時間
    val iconHeartRate: MutableLiveData<Drawable> = MutableLiveData(null),//心率圖示
    var heartRate: MutableLiveData<String> = MutableLiveData(""),//心率 bpm
    var heartRateTextColor: MutableLiveData<Int> = MutableLiveData(-1),//心率顏色
    val iconBreathState: MutableLiveData<Drawable> = MutableLiveData(null),//呼吸頻率圖示
    var breathState: MutableLiveData<String> = MutableLiveData(""),//呼吸頻率 brpm
    var breathStateTextColor: MutableLiveData<Int> = MutableLiveData(-1),//呼吸頻率顏色
    val iconBodyTemperature: MutableLiveData<Drawable> = MutableLiveData(null),//呼吸頻率圖示
    var bodyTemperature: MutableLiveData<String> = MutableLiveData(""),//體溫
    var bodyTemperatureTextColor: MutableLiveData<Int> = MutableLiveData(-1),//體溫狀態顏色
    var isHasTemperature :MutableLiveData<Boolean> = MutableLiveData(false),//是否有溫度裝置
    val isShowRefresh: MutableLiveData<Boolean> = MutableLiveData(false)//是否有顯示Refresh按鈕
//    var iconBedStatus: Drawable? = null,//中間圖示
//    var frameBedStatus: Drawable? = null,//外框圖示
//    var textBedStatus: String = "",//床上狀態
//    var textColorBedStatus: Int? = null,//床上狀態顏色
//    var time: String ="",//資料時間
//    var heartRate: String = "",//心率 bpm
//    var breathState: String = "",//呼吸頻率 brpm
){
    companion object {
        const val BODY_TEMPERATURE_ERROR = "-1"
    }
}
