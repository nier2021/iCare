package com.docter.icare.data.entities.view

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData

data class BedsideMonitorEntity(
    val iconBedStatus: MutableLiveData<Drawable> = MutableLiveData(null),//中間圖示
    val frameBedStatus: MutableLiveData<Drawable> = MutableLiveData(null),//外框圖示
    val textBedStatus: MutableLiveData<String> = MutableLiveData(""),//床上狀態
    var textColorBedStatus: MutableLiveData<Int> = MutableLiveData(-1),//床上狀態顏色
    var time: MutableLiveData<String> = MutableLiveData(""),//資料時間
    var heartRate: MutableLiveData<String> = MutableLiveData(""),//心率 bpm
    var breathState: MutableLiveData<String> = MutableLiveData(""),//呼吸頻率 brpm
//    var iconBedStatus: Drawable? = null,//中間圖示
//    var frameBedStatus: Drawable? = null,//外框圖示
//    var textBedStatus: String = "",//床上狀態
//    var textColorBedStatus: Int? = null,//床上狀態顏色
//    var time: String ="",//資料時間
//    var heartRate: String = "",//心率 bpm
//    var breathState: String = "",//呼吸頻率 brpm
)
