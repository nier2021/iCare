package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BindingDeviceResponse(
    @SerializedName("success")
    val success: Int,

//    @SerializedName("sid")
//    val sid: String,

    @SerializedName("message")
    val message: String,

//    @SerializedName("type")
//    val type: Int,

//    @SerializedName("serialNumber")
//    val serialNumber: String,
//
//    @SerializedName("deviceType")
//    val deviceType: String,

    @SerializedName("id")
    val accountId: String
): Parcelable
