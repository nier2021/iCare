package com.docter.icare.data.repositories

import android.util.Log
import com.docter.icare.R
import com.docter.icare.data.entities.view.LoginEntity
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.*
import com.docter.icare.utils.InputException
import com.docter.icare.utils.NoInternetException
import com.docter.icare.utils.PermissionCheckUtils
import com.docter.icare.utils.SidException

class LoginRepository (
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val api: ApiService
) : SafeApiRequest(resource) {
    fun checkLoginInput(
        entity: LoginEntity
    ) {
//        Log.i("LoginRepository","checkLoginInput")
//        Log.i("LoginRepository","entity account=>${entity.account}")
//        Log.i("LoginRepository","entity password=>${entity.password}")
        when {
            entity.account.isBlank() -> throw InputException(resource.getString(R.string.error_account_empty))
            entity.account.length < 6 || entity.account.length > 20 -> throw InputException(resource.getString(R.string.error_account_password_length)+"( ${resource.getString(R.string.account)} )")

            entity.password.isBlank() -> throw InputException(resource.getString(R.string.error_password_empty))
            entity.password.length < 6 || entity.password.length > 32 -> throw InputException(resource.getString(R.string.error_account_password_length)+"( ${resource.getString(R.string.password)} )")
        }
    }


    suspend fun login(
        entity: LoginEntity
    ) = apiRequest { api.login(entity.account, entity.password) }.let {
        if (it.sid.isNotBlank()) {
//            Log.i("LoginRepository","account=>${entity.account} \n sid=>${it.sid} \n name=>${it.name}")
            with(preference){
                set(ACCOUNT, entity.account)
                set(PASSWORD, entity.password)
                set(SID, it.sid)
                set(NAME, it.name)
            }
        }
    }
}