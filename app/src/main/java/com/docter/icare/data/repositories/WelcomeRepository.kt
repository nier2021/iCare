package com.docter.icare.data.repositories

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import androidx.core.app.ActivityCompat
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.ACCOUNT_ID
import com.docter.icare.data.resource.NAME
import com.docter.icare.data.resource.ResourceProvider
import com.docter.icare.data.resource.*
import com.docter.icare.utils.PermissionCheckUtils

class WelcomeRepository (
    private val permission: PermissionCheckUtils,
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val api: ApiService
) : SafeApiRequest(resource) {

    companion object {
        const val PERMISSION = 0
        const val BACKGROUND_LOCATION_PERMISSION = 1
    }

    //Welcome
//    val permissionArray = arrayOf(
//        CAMERA,
//        WRITE_EXTERNAL_STORAGE,
//        READ_EXTERNAL_STORAGE,
//        ACCESS_COARSE_LOCATION,
//        ACCESS_FINE_LOCATION,
//        ACCESS_WIFI_STATE,
//        CHANGE_NETWORK_STATE,
////        WRITE_SETTINGS
////        BLUETOOTH_SCAN,
////        BLUETOOTH_CONNECT,
////        BLUETOOTH_ADVERTISE//要判斷12
//
//    )
//    // RECORD_AUDIO,
//    //            CAMERA,
//    //            ACCESS_COARSE_LOCATION,
//    //            ACCESS_FINE_LOCATION,
//    ////            WRITE_EXTERNAL_STORAGE,
//    //            READ_EXTERNAL_STORAGE,
//    //未來有需求改變
//    val permissionArrayVerS = arrayOf(
//        CAMERA,
//        WRITE_EXTERNAL_STORAGE,
//        READ_EXTERNAL_STORAGE,
//        ACCESS_COARSE_LOCATION,
//        ACCESS_FINE_LOCATION,
//        BLUETOOTH_ADVERTISE,
//        BLUETOOTH_SCAN,
//        BLUETOOTH_CONNECT,
//        ACCESS_WIFI_STATE,
//        CHANGE_NETWORK_STATE,
//
//
////        WRITE_SETTINGS
//    )

    val permissionArray = when {

        SDK_INT < S -> arrayOf(
            CAMERA,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,
            ACCESS_WIFI_STATE,
            CHANGE_NETWORK_STATE,
            //        WRITE_SETTINGS
//        BLUETOOTH_SCAN,
//        BLUETOOTH_CONNECT,
//        BLUETOOTH_ADVERTISE//要判斷12
        )

        else -> arrayOf(
            CAMERA,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,
            BLUETOOTH_ADVERTISE,
            BLUETOOTH_SCAN,
            BLUETOOTH_CONNECT,
            ACCESS_WIFI_STATE,
            CHANGE_NETWORK_STATE,
        )
    }

    fun isNeedAskPermission() = permission.checkSelfPermission(permissionArray)

    fun isNeedAskPermission(
        grantResults: IntArray
    ) = when {
        grantResults.isEmpty() -> true
        else -> grantResults.any { it != PackageManager.PERMISSION_GRANTED }
    }

//    fun isLoggedIn() = preference.getString(SID).isNotBlank()

    fun isLoggedIn() = preference.getString(TOKEN).isNotBlank()

}