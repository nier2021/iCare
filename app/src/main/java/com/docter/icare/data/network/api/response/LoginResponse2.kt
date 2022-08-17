package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse2(

    @SerializedName("sid")
    val sid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("message")
    val message: String

) : Parcelable
