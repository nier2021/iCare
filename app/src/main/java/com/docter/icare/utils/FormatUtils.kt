package com.docter.icare.utils

import android.content.Context
import com.docter.icare.R

fun Int.toBedName(context: Context): String = when(this){
    2 -> context.getString(R.string.bed_type_2_text)
    3 -> context.getString(R.string.bed_type_3_text)
    4 -> context.getString(R.string.bed_type_4_text)
    else -> context.getString(R.string.bed_type_1_text)
}


fun ByteArray.toHexStringSpace(): String = when {

    isEmpty() -> ""

    else -> StringBuilder().apply {

        this@toHexStringSpace.forEach {

            Integer.toHexString(it.toInt().and(0xFF)).let { hex ->

                append(when {
                    hex.length < 2 -> "0"
                    else -> ""
                })

                append(hex).append(" ")
            }
        }

    }.toString()
}

fun String.isSpecialSymbols() :Boolean = Regex("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]").matches(this)


