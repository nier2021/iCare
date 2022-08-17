package com.docter.icare.data.repositories

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.ACCOUNT_ID
import com.docter.icare.data.resource.NAME
import com.docter.icare.data.resource.ResourceProvider
import com.docter.icare.data.resource.SID
import com.docter.icare.utils.PermissionCheckUtils

class WelcomeRepository (
    private val permission: PermissionCheckUtils,
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val api: ApiService
) : SafeApiRequest(resource) {
    //Welcome
    val permissionArray = arrayOf(
        CAMERA,
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION
    )
    //未來有需求改變
    val permissionArrayVerS = arrayOf(
        CAMERA,
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        BLUETOOTH_SCAN,
        BLUETOOTH_CONNECT
    )

    fun isNeedAskPermission() =
        if (SDK_INT < S){
            permission.checkSelfPermission(permissionArray)
        }else permission.checkSelfPermission(permissionArrayVerS)

    fun isNeedAskPermission(
        grantResults: IntArray
    ) = when {
        grantResults.isEmpty() -> true
        else -> grantResults.any { it != PackageManager.PERMISSION_GRANTED }
    }

    fun isLoggedIn() = preference.getString(SID).isNotBlank()

}