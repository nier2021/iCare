package com.docter.icare.data.resource

import android.content.Context
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable

class ResourceProvider(
    context: Context
) {

    private val appContext = context.applicationContext

    fun getString(
        resId: Int
    ) = appContext.getString(resId)

    fun getString(
        resId: Int,
        formatArgs: Int
    ) = appContext.getString(resId, formatArgs)

    fun getString(
        resId: Int,
        formatArgs: Float
    ) = appContext.getString(resId, formatArgs)

    fun getString(
        resId: Int,
        formatArgs: String
    ) = appContext.getString(resId, formatArgs)

    fun getIntArray(
        resId: Int
    ): IntArray = appContext.resources.getIntArray(resId)

    fun getStringArray(
        resId: Int
    ): Array<String> = appContext.resources.getStringArray(resId)

    fun getColor(
        resId: Int
    ) = getColor(appContext, resId)

    fun getDimension(
        resId: Int
    ) = appContext.resources.getDimension(resId)

    fun getDrawable(
        resId: Int
    ) = getDrawable(appContext, resId)

}