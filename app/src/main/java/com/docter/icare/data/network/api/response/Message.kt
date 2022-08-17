package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(

    @SerializedName("ch")
    val ch: String,

    @SerializedName("en")
    val en: String

) : Parcelable
