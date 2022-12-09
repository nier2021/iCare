package com.docter.icare.data.network.api

import android.content.Context
import com.docter.icare.R

const val CALL_DEVICE_TYPE_ANDROID = "0"

fun String.getToken(): String = "Bearer $this"

fun String.apiErrorShow(context: Context): Pair<Boolean ,String> =
    when(this){
        "password error","error account" -> Pair(false, context.getString(R.string.error_login_acc_pass_incorrect))
        "unauthenticated","Unauthenticated" ->  Pair(true, context.getString(R.string.response_unauthenticated))
        else ->Pair(false, this)
    }