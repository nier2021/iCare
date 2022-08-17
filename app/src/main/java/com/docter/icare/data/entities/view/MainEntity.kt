package com.docter.icare.data.entities.view

import androidx.lifecycle.MutableLiveData

data class MainEntity(
    val title: MutableLiveData<String> = MutableLiveData(""),

    val name: MutableLiveData<String> = MutableLiveData(""),

//    val nowFragment: MutableLiveData<Int> = MutableLiveData(BEDSIDE_MONITOR),

    val isDrawerBottomViewShow: MutableLiveData<Boolean> = MutableLiveData(true)

){

    companion object {
        const val BEDSIDE_MONITOR = 0
        const val AIR_QUALITY_INDEX = 1
        const val ACTIVITY_MONITORING = 2
        const val PERSON_INFO = 3
//        const val VIP = 4
        const val DEVICE = 5
//        const val MEMBER_CENTRE = 6
//        const val VIDEO_CALL = 7
//        const val BLOOD_PRESSURE = 8
//        const val BLOOD_SUGAR = 9
//        const val ACTIVE = 10
//        const val SLEEP = 11
//        const val EXERCISE = 12
//        const val HEART_HEALTH = 13
//        const val SUGAR_ANALYSIS = 14
//        const val ECG = 15
//        const val URIC_ACID = 16
//        const val CHOLESTEROL = 17
//        const val SPO2 = 18
//        const val TEMPERATURE = 19
//        const val MEDICATION = 20
//        const val DIET = 21
//        const val SYMPTOM = 22
//        const val WATER = 23
    }
}
