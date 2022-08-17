package com.docter.icare.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager.getDefaultSharedPreferences

class PreferenceProvider (
    context: Context
) {

    private val appContext = context.applicationContext

    private val sp: SharedPreferences = getDefaultSharedPreferences(appContext)

    fun set(
        key: String,
        value: Int
    ) {
        sp.edit().putInt(key, value).apply()
    }

    fun set(
        key: String,
        value: Long
    ) {
        sp.edit().putLong(key, value).apply()
    }

    fun set(
        key: String,
        value: Float
    ) {
        sp.edit().putFloat(key, value).apply()
    }

    fun set(
        key: String,
        value: String
    ) {
        sp.edit().putString(key, value).apply()
    }

    fun set(
        key: String,
        value: Boolean
    ) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getInt(
        key: String
    ) = sp.getInt(key, -1)

    fun getInt(
        key: String,
        default: Int
    ) = sp.getInt(key, default)

    fun getLong(
        key: String
    ) = sp.getLong(key, -1L)

    fun getLong(
        key: String,
        default: Long
    ) = sp.getLong(key, default)

    fun getFloat(
        key: String
    ) = sp.getFloat(key, -1f)

    fun getFloat(
        key: String,
        default: Float
    ) = sp.getFloat(key, default)

    fun getString(
        key: String
    ) = sp.getString(key, "")!!

    fun getString(
        key: String,
        default: String
    ) = sp.getString(key, default)!!

    fun getBoolean(
        key: String
    ) = sp.getBoolean(key, false)

    fun getBoolean(
        key: String,
        default: Boolean
    ) = sp.getBoolean(key, default)

    fun clear(): Boolean {

        sp.edit().clear().apply()

        return true
    }
}