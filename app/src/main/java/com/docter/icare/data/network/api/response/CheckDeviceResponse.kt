package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckDeviceResponse(
    @SerializedName("success")
    val success: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<DeviceInfo>,

): Parcelable{
    @Parcelize
    data class DeviceInfo(

        @SerializedName("serial_number")
        val serialNumber: String,

        @SerializedName("device_type")
        val deviceType: Int,

        @SerializedName("mac_address")//未定,到時看小張命名啥
        val macAddress: String ,

        @SerializedName("accountId")
        val accountId: Int,

    ) : Parcelable
}
