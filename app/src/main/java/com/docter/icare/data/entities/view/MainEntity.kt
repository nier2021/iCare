package com.docter.icare.data.entities.view

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData

data class MainEntity(
    val title: MutableLiveData<String> = MutableLiveData(""),

    val name: MutableLiveData<String> = MutableLiveData(""),

    val nowFragment: MutableLiveData<Int> = MutableLiveData(BEDSIDE_MONITOR),

    val isDrawerBottomViewShow: MutableLiveData<Boolean> = MutableLiveData(true)

){

    companion object {
        const val BEDSIDE_MONITOR = 0
        const val AIR_QUALITY_INDEX = 1
        const val ACTIVITY_MONITORING = 2
        const val EXCEPTION =3
        const val DEVICE_SCAN = 4
        const val DEVICE = 5
        const val PERSON_INFO_CONTENT = 6
        const val PERSON_INFO_EDIT= 7
        const val SET = 8
    }
}
