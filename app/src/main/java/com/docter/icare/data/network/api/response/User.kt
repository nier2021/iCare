package com.docter.icare.data.network.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("account")
    val account: String,

    @SerializedName("account_id")
    val accountId: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("parent_id")
    val parentId: Int,//身分證

    @SerializedName("level")
    val level: Int,

    @SerializedName("birthday")
    val birthday: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("phone")
    val phone: String,
): Parcelable