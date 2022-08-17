package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    @SerializedName("success")
    val success: Int,

    @SerializedName("message")
    val message: Message,

    @SerializedName("sid")
    val sid: String

) : Parcelable
